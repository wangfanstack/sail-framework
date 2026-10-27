package cn.sailstack.module.system.framework.sms.core.client.impl;

import cn.sailstack.framework.common.core.KeyValue;
import cn.sailstack.framework.common.util.collection.MapUtils;
import cn.sailstack.framework.common.util.http.HttpUtils;
import cn.sailstack.framework.common.util.json.JsonUtils;
import cn.sailstack.framework.common.util.string.DigestUtils;
import cn.sailstack.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.sailstack.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.sailstack.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.sailstack.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.sailstack.module.system.framework.sms.core.property.SmsChannelProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import static cn.sailstack.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 阿里短信客户端的实现类
 *
 * @author zzf
 * @since 2021/1/25 14:17
 */
@Slf4j
public class AliyunSmsClient extends AbstractSmsClient {

    private static final String URL = "https://dysmsapi.aliyuncs.com";
    private static final String HOST = "dysmsapi.aliyuncs.com";
    private static final String VERSION = "2017-05-25";

    private static final String RESPONSE_CODE_SUCCESS = "OK";

    private static final DateTimeFormatter GMT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(java.time.ZoneId.of("GMT"));

    public AliyunSmsClient(SmsChannelProperties properties) {
        super(properties);
        if (properties.getApiKey() == null || properties.getApiKey().isEmpty()) {
            throw new IllegalArgumentException("apiKey 不能为空");
        }
        if (properties.getApiSecret() == null || properties.getApiSecret().isEmpty()) {
            throw new IllegalArgumentException("apiSecret 不能为空");
        }
    }

    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile, String apiTemplateId,
                                  List<KeyValue<String, Object>> templateParams) throws Throwable {
        if (properties.getSignature() == null || properties.getSignature().trim().isEmpty()) {
            throw new IllegalArgumentException("短信签名不能为空");
        }
        // 1. 执行请求
        // 参考链接 https://api.aliyun.com/document/Dysmsapi/2017-05-25/SendSms
        TreeMap<String, Object> queryParam = new TreeMap<>();
        queryParam.put("PhoneNumbers", mobile);
        queryParam.put("SignName", properties.getSignature());
        queryParam.put("TemplateCode", apiTemplateId);
        queryParam.put("TemplateParam", JsonUtils.toJsonString(MapUtils.convertMap(templateParams)));
        queryParam.put("OutId", sendLogId);
        JsonNode response = request("SendSms", queryParam);

        // 2. 解析请求
        return new SmsSendRespDTO()
                .setSuccess(Objects.equals(JsonUtils.getText(response, "Code"), RESPONSE_CODE_SUCCESS))
                .setSerialNo(JsonUtils.getText(response, "BizId"))
                .setApiRequestId(JsonUtils.getText(response, "RequestId"))
                .setApiCode(JsonUtils.getText(response, "Code"))
                .setApiMsg(JsonUtils.getText(response, "Message"));
    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        List<JsonNode> statuses = JsonUtils.parseArray(text, JsonNode.class);
        // 字段参考 https://help.aliyun.com/zh/sms/developer-reference/smsreport-2
        return convertList(statuses, status -> new SmsReceiveRespDTO()
                .setSuccess(getBoolean(status, "success")) // 是否接收成功
                .setErrorCode(JsonUtils.getText(status, "err_code")) // 状态报告编码
                .setErrorMsg(JsonUtils.getText(status, "err_msg")) // 状态报告说明
                .setMobile(JsonUtils.getText(status, "phone_number")) // 手机号
                .setReceiveTime(getLocalDateTime(status, "report_time")) // 状态报告时间
                .setSerialNo(JsonUtils.getText(status, "biz_id")) // 发送序列号
                .setLogId(getLong(status, "out_id"))); // 用户序列号
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) throws Throwable {
        // 1. 执行请求
        // 参考链接 https://api.aliyun.com/document/Dysmsapi/2017-05-25/GetSmsTemplate
        TreeMap<String, Object> queryParam = new TreeMap<>();
        queryParam.put("TemplateCode", apiTemplateId);
        JsonNode response = request("GetSmsTemplate", queryParam);

        // 2.1 请求失败
        String code = JsonUtils.getText(response, "Code");
        if (!Objects.equals(code, RESPONSE_CODE_SUCCESS)) {
            log.error("[getSmsTemplate][模版编号({}) 响应不正确({})]", apiTemplateId, response);
            return null;
        }
        // 2.2 请求成功
        return new SmsTemplateRespDTO()
                .setId(JsonUtils.getText(response, "TemplateCode"))
                .setContent(JsonUtils.getText(response, "TemplateContent"))
                .setAuditStatus(convertSmsTemplateAuditStatus(getInteger(response, "TemplateStatus")))
                .setAuditReason(JsonUtils.getText(response, "Reason"));
    }

    @VisibleForTesting
    @SuppressWarnings("EnhancedSwitchMigration")
    Integer convertSmsTemplateAuditStatus(Integer templateStatus) {
        switch (templateStatus) {
            case 0: return SmsTemplateAuditStatusEnum.CHECKING.getStatus();
            case 1: return SmsTemplateAuditStatusEnum.SUCCESS.getStatus();
            case 2: return SmsTemplateAuditStatusEnum.FAIL.getStatus();
            default: throw new IllegalArgumentException(String.format("未知审核状态(%d)", templateStatus));
        }
    }

    /**
     * 请求阿里云短信
     *
     * @see <a href="https://help.aliyun.com/zh/sdk/product-overview/v3-request-structure-and-signature">V3 版本请求体&签名机制</>
     * @param apiName 请求的 API 名称
     * @param queryParams 请求参数
     * @return 请求结果
     */
    private JsonNode request(String apiName, TreeMap<String, Object> queryParams) {
        // 1. 请求参数
        String queryString = queryParams.entrySet().stream()
                .map(entry -> percentCode(entry.getKey()) + "=" + percentCode(String.valueOf(entry.getValue())))
                .collect(Collectors.joining("&"));

        // 2. 请求 Body
        String requestBody = ""; // 短信 API 为 RPC 接口，query parameters 在 uri 中拼接，因此 request body 如果没有特殊要求，设置为空
        String hashedRequestPayload = DigestUtils.sha256Hex(requestBody);

        // 3.1 请求 Header
        TreeMap<String, String> headers = new TreeMap<>();
        headers.put("host", HOST);
        headers.put("x-acs-version", VERSION);
        headers.put("x-acs-action", apiName);
        headers.put("x-acs-date", GMT_DATE_TIME_FORMATTER.format(java.time.Instant.now()));
        headers.put("x-acs-signature-nonce", UUID.randomUUID().toString());
        headers.put("x-acs-content-sha256", hashedRequestPayload);

        // 3.2 构建签名 Header
        StringBuilder canonicalHeaders = new StringBuilder(); // 构造请求头，多个规范化消息头，按照消息头名称（小写）的字符代码顺序以升序排列后拼接在一起
        StringBuilder signedHeadersBuilder = new StringBuilder(); // 已签名消息头列表，多个请求头名称（小写）按首字母升序排列并以英文分号（;）分隔
        headers.entrySet().stream().filter(entry -> entry.getKey().toLowerCase().startsWith("x-acs-")
                        || "host".equalsIgnoreCase(entry.getKey())
                        || "content-type".equalsIgnoreCase(entry.getKey()))
                .sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    String lowerKey = entry.getKey().toLowerCase();
                    canonicalHeaders.append(lowerKey).append(":").append(String.valueOf(entry.getValue()).trim()).append("\n");
                    signedHeadersBuilder.append(lowerKey).append(";");
                });
        String signedHeaders = signedHeadersBuilder.substring(0, signedHeadersBuilder.length() - 1);

        // 4. 构建 Authorization 签名
        String canonicalRequest = "POST" + "\n" +
                "/" + "\n" +
                queryString + "\n" +
                canonicalHeaders + "\n" +
                signedHeaders + "\n" +
                hashedRequestPayload;
        String hashedCanonicalRequest = DigestUtils.sha256Hex(canonicalRequest);
        String stringToSign = "ACS3-HMAC-SHA256" + "\n" + hashedCanonicalRequest;
        String signature = hmacSha256Hex(properties.getApiSecret(), stringToSign); // 计算签名
        headers.put("Authorization", "ACS3-HMAC-SHA256" + " " + "Credential=" + properties.getApiKey()
                + ", " + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature);

        // 5. 发起请求
        String responseBody = HttpUtils.post(URL + "?" + queryString, headers, requestBody);
        return JsonUtils.parseTree(responseBody);
    }

    /**
     * 对指定的字符串进行 URL 编码，并对特定的字符进行替换，以符合URL编码规范
     *
     * @param str 需要进行 URL 编码的字符串
     * @return 编码后的字符串
     */
    @SneakyThrows
    private static String percentCode(String str) {
        if (str == null) {
            throw new IllegalArgumentException("str 不能为空");
        }
        return HttpUtils.encodeUtf8(str)
                .replace("+", "%20") // 加号 "+" 被替换为 "%20"
                .replace("*", "%2A") // 星号 "*" 被替换为 "%2A"
                .replace("%7E", "~"); // 波浪号 "%7E" 被替换为 "~"
    }

    private static String hmacSha256Hex(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("计算 HmacSHA256 签名失败", e);
        }
    }

    static Boolean getBoolean(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    static Long getLong(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        return value == null || value.isNull() ? null : value.asLong();
    }

    static Integer getInteger(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        return value == null || value.isNull() ? null : value.asInt();
    }

    static LocalDateTime getLocalDateTime(JsonNode node, String fieldName) {
        String text = JsonUtils.getText(node, fieldName);
        if (text == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(text);
        } catch (Exception ignore) {
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

}
