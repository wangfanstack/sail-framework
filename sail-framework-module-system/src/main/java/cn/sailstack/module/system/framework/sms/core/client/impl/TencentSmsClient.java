package cn.sailstack.module.system.framework.sms.core.client.impl;

import cn.sailstack.framework.common.core.KeyValue;
import cn.sailstack.framework.common.util.collection.ArrayUtils;
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
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static cn.sailstack.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 腾讯云短信功能实现
 *
 * 参见 <a href="https://cloud.tencent.com/document/product/382/52077">文档</a>
 *
 * @author shiwp
 */
public class TencentSmsClient extends AbstractSmsClient {

    private static final String HOST = "sms.tencentcloudapi.com";
    private static final String VERSION = "2021-01-11";
    private static final String REGION = "ap-guangzhou";

    private static final DateTimeFormatter UTC_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"));

    /**
     * 调用成功 code
     */
    public static final String API_CODE_SUCCESS = "Ok";

    /**
     * 是否国际/港澳台短信：
     *
     * 0：表示国内短信。
     * 1：表示国际/港澳台短信。
     */
    private static final long INTERNATIONAL_CHINA = 0L;

    public TencentSmsClient(SmsChannelProperties properties) {
        super(properties);
        if (properties.getApiSecret() == null || properties.getApiSecret().isEmpty()) {
            throw new IllegalArgumentException("apiSecret 不能为空");
        }
        validateSdkAppId(properties);
    }

    /**
     * 参数校验腾讯云的 SDK AppId
     *
     * 原因是：腾讯云发放短信的时候，需要额外的参数 sdkAppId
     *
     * 解决方案：考虑到不破坏原有的 apiKey + apiSecret 的结构，所以将 secretId 拼接到 apiKey 字段中，格式为 "secretId sdkAppId"。
     *
     * @param properties 配置
     */
    private static void validateSdkAppId(SmsChannelProperties properties) {
        String combineKey = properties.getApiKey();
        if (combineKey == null || combineKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey 不能为空");
        }
        String[] keys = combineKey.trim().split(" ");
        if (keys.length != 2) {
            throw new IllegalArgumentException("腾讯云短信 apiKey 配置格式错误，请配置 为[secretId sdkAppId]");
        }
    }

    private String getSdkAppId() {
        return StringUtils.substringAfterLast(properties.getApiKey(), " ");
    }

    private String getApiKey() {
        return StringUtils.substringBeforeLast(properties.getApiKey(), " ");
    }

    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile,
                                  String apiTemplateId, List<KeyValue<String, Object>> templateParams) throws Throwable {
        // 1. 执行请求
        // 参考链接 https://cloud.tencent.com/document/product/382/55981
        TreeMap<String, Object> body = new TreeMap<>();
        body.put("PhoneNumberSet", new String[]{mobile});
        body.put("SmsSdkAppId", getSdkAppId());
        body.put("SignName", properties.getSignature());
        body.put("TemplateId", apiTemplateId);
        body.put("TemplateParamSet", ArrayUtils.toArray(templateParams, param -> String.valueOf(param.getValue())));
        JsonNode response = request("SendSms", body);

        // 2. 解析请求
        JsonNode responseResult = response.get("Response");
        JsonNode error = responseResult.get("Error");
        if (error != null && !error.isNull()) {
            return new SmsSendRespDTO().setSuccess(false)
                    .setApiRequestId(JsonUtils.getText(responseResult, "RequestId"))
                    .setApiCode(JsonUtils.getText(error, "Code"))
                    .setApiMsg(JsonUtils.getText(error, "Message"));
        }
        JsonNode sendResult = responseResult.get("SendStatusSet").get(0);
        return new SmsSendRespDTO().setSuccess(Objects.equals(API_CODE_SUCCESS, JsonUtils.getText(sendResult, "Code")))
                .setApiRequestId(JsonUtils.getText(responseResult, "RequestId"))
                .setSerialNo(JsonUtils.getText(sendResult, "SerialNo"))
                .setApiMsg(JsonUtils.getText(sendResult, "Message"));
    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        List<JsonNode> statuses = JsonUtils.parseArray(text, JsonNode.class);
        // 字段参考
        return convertList(statuses, statusObj -> new SmsReceiveRespDTO()
                .setSuccess("SUCCESS".equals(JsonUtils.getText(statusObj, "report_status"))) // 是否接收成功
                .setErrorCode(JsonUtils.getText(statusObj, "errmsg")) // 状态报告编码
                .setErrorMsg(JsonUtils.getText(statusObj, "description")) // 状态报告描述
                .setMobile(JsonUtils.getText(statusObj, "mobile")) // 手机号
                .setReceiveTime(AliyunSmsClient.getLocalDateTime(statusObj, "user_receive_time")) // 状态报告时间
                .setSerialNo(JsonUtils.getText(statusObj, "sid"))); // 发送序列号
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) throws Throwable {
        // 1. 构建请求
        // 参考链接 https://cloud.tencent.com/document/product/382/52067
        TreeMap<String, Object> body = new TreeMap<>();
        body.put("International", INTERNATIONAL_CHINA);
        body.put("TemplateIdSet", new Integer[]{Integer.valueOf(apiTemplateId)});
        JsonNode response = request("DescribeSmsTemplateList", body);

        // 2. 解析请求
        JsonNode statusResult = response.get("Response")
                .get("DescribeTemplateStatusSet").get(0);
        return new SmsTemplateRespDTO().setId(apiTemplateId)
                .setContent(statusResult.get("TemplateContent").toString())
                .setAuditStatus(convertSmsTemplateAuditStatus(AliyunSmsClient.getInteger(statusResult, "StatusCode")))
                .setAuditReason(statusResult.get("ReviewReply").toString());
    }

    @VisibleForTesting
    Integer convertSmsTemplateAuditStatus(int templateStatus) {
        switch (templateStatus) {
            case 1: return SmsTemplateAuditStatusEnum.CHECKING.getStatus();
            case 0: return SmsTemplateAuditStatusEnum.SUCCESS.getStatus();
            case -1: return SmsTemplateAuditStatusEnum.FAIL.getStatus();
            default: throw new IllegalArgumentException(String.format("未知审核状态(%d)", templateStatus));
        }
    }

    /**
     * 请求腾讯云短信
     *
     * @see <a href="https://cloud.tencent.com/document/product/382/52072">签名方法 v3</a>
     *
     * @param action 请求的 API 名称
     * @param body 请求参数
     * @return 请求结果
     */
    private JsonNode request(String action, TreeMap<String, Object> body) {
        // 1.1 请求 Header
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Host", HOST);
        headers.put("X-TC-Action", action);
        Instant now = Instant.now();
        String nowStr = UTC_DATE_FORMATTER.format(now);
        headers.put("X-TC-Timestamp", String.valueOf(now.toEpochMilli() / 1000));
        headers.put("X-TC-Version", VERSION);
        headers.put("X-TC-Region", REGION);

        // 1.2 构建签名 Header
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json; charset=utf-8\n"
                + "host:" + HOST + "\n" + "x-tc-action:" + action.toLowerCase() + "\n";
        String signedHeaders = "content-type;host;x-tc-action";
        String canonicalRequest = "POST" + "\n" + "/" + "\n" + canonicalQueryString + "\n" + canonicalHeaders + "\n"
                + signedHeaders + "\n" + DigestUtils.sha256Hex(JsonUtils.toJsonString(body));
        String credentialScope = nowStr + "/" + "sms" + "/" + "tc3_request";
        String stringToSign = "TC3-HMAC-SHA256" + "\n" + now.toEpochMilli() / 1000 + "\n" + credentialScope + "\n" +
                DigestUtils.sha256Hex(canonicalRequest);
        byte[] secretService = hmac256(hmac256(("TC3" + properties.getApiSecret()).getBytes(StandardCharsets.UTF_8), nowStr), "sms");
        String signature = encodeHex(hmac256(hmac256(secretService, "tc3_request"), stringToSign));
        headers.put("Authorization", "TC3-HMAC-SHA256" + " " + "Credential=" + getApiKey() + "/" + credentialScope + ", "
                + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature);

        // 2. 发起请求
        String responseBody = HttpUtils.post("https://" + HOST, headers, JsonUtils.toJsonString(body));
        return JsonUtils.parseTree(responseBody);
    }

    private static byte[] hmac256(byte[] key, String msg) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("计算 HmacSHA256 签名失败", e);
        }
    }

    private static String encodeHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
