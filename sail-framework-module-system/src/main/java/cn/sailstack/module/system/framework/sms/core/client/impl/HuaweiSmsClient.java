package cn.sailstack.module.system.framework.sms.core.client.impl;

import cn.sailstack.framework.common.core.KeyValue;
import cn.sailstack.framework.common.util.http.HttpUtils;
import cn.sailstack.framework.common.util.json.JsonUtils;
import cn.sailstack.framework.common.util.string.DigestUtils;
import cn.sailstack.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.sailstack.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.sailstack.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.sailstack.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.sailstack.module.system.framework.sms.core.property.SmsChannelProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static cn.sailstack.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 华为短信客户端的实现类
 *
 * @author scholar
 * @since 2024/6/02 11:55
 */
@Slf4j
public class HuaweiSmsClient extends AbstractSmsClient {

    private static final String URL = "https://smsapi.cn-north-4.myhuaweicloud.com:443/sms/batchSendSms/v1";//APP接入地址+接口访问URI
    private static final String HOST = "smsapi.cn-north-4.myhuaweicloud.com:443";
    private static final String SIGNEDHEADERS = "content-type;host;x-sdk-date";

    private static final String RESPONSE_CODE_SUCCESS = "000000";

    private static final DateTimeFormatter SDK_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneId.of("UTC"));

    public HuaweiSmsClient(SmsChannelProperties properties) {
        super(properties);
        if (properties.getApiKey() == null || properties.getApiKey().isEmpty()) {
            throw new IllegalArgumentException("apiKey 不能为空");
        }
        if (properties.getApiSecret() == null || properties.getApiSecret().isEmpty()) {
            throw new IllegalArgumentException("apiSecret 不能为空");
        }
        validateSender(properties);
    }

    /**
     * 参数校验华为云的 sender 通道号
     *
     * 原因是：验华为云发放短信的时候，需要额外的参数 sender
     *
     * 解决方案：考虑到不破坏原有的 apiKey + apiSecret 的结构，所以将 secretId 拼接到 apiKey 字段中，格式为 "secretId sdkAppId"。
     *
     * @param properties 配置
     */
    private static void validateSender(SmsChannelProperties properties) {
        String combineKey = properties.getApiKey();
        if (combineKey == null || combineKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey 不能为空");
        }
        String[] keys = combineKey.trim().split(" ");
        if (keys.length != 2) {
            throw new IllegalArgumentException("华为云短信 apiKey 配置格式错误，请配置 为[accessKeyId sender]");
        }
    }

    private String getAccessKey() {
        return StringUtils.substringBeforeLast(properties.getApiKey(), " ");
    }

    private String getSender() {
        return StringUtils.substringAfterLast(properties.getApiKey(), " ");
    }

    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile, String apiTemplateId,
                                  List<KeyValue<String, Object>> templateParams) throws Throwable {
        StringBuilder requestBody = new StringBuilder();
        appendToBody(requestBody, "from=", getSender());
        appendToBody(requestBody, "&to=", mobile);
        appendToBody(requestBody, "&templateId=", apiTemplateId);
        appendToBody(requestBody, "&templateParas=", JsonUtils.toJsonString(
                convertList(templateParams, kv -> String.valueOf(kv.getValue()))));
        appendToBody(requestBody, "&statusCallback=", properties.getCallbackUrl());
        appendToBody(requestBody, "&extend=", String.valueOf(sendLogId));
        JsonNode response = request("/sms/batchSendSms/v1/", "POST", requestBody.toString());

        // 2. 解析请求
        if (!response.has("result")) { // 例如说：密钥不正确
            return new SmsSendRespDTO().setSuccess(false)
                    .setApiCode(JsonUtils.getText(response, "code"))
                    .setApiMsg(JsonUtils.getText(response, "description"));
        }
        JsonNode sendResult = response.get("result").get(0);
        return new SmsSendRespDTO().setSuccess(RESPONSE_CODE_SUCCESS.equals(JsonUtils.getText(response, "code")))
                .setSerialNo(JsonUtils.getText(sendResult, "smsMsgId")).setApiCode(JsonUtils.getText(sendResult, "status"));
    }

    /**
     * 请求华为云短信
     *
     * @see <a href="认证鉴权">https://support.huaweicloud.com/api-msgsms/sms_05_0046.html</a>
     * @param uri 请求 URI
     * @param method 请求 Method
     * @param requestBody 请求 Body
     * @return 请求结果
     */
    private JsonNode request(String uri, String method, String requestBody) {
        // 1.1 请求 Header
        TreeMap<String, String> headers = new TreeMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        String sdkDate = SDK_DATE_FORMATTER.format(Instant.now());
        headers.put("X-Sdk-Date", sdkDate);
        headers.put("host", HOST);

        // 1.2 构建签名 Header
        String canonicalQueryString = ""; // 查询参数为空
        String canonicalHeaders = "content-type:application/x-www-form-urlencoded\n"
                + "host:"+ HOST +"\n" + "x-sdk-date:" + sdkDate + "\n";
        String canonicalRequest = method + "\n" + uri + "\n" + canonicalQueryString + "\n"
                + canonicalHeaders + "\n" + SIGNEDHEADERS + "\n" + DigestUtils.sha256Hex(requestBody);
        String stringToSign = "SDK-HMAC-SHA256" + "\n" + sdkDate + "\n" + DigestUtils.sha256Hex(canonicalRequest);
        String signature = hmacSha256Hex(properties.getApiSecret(), stringToSign);  // 计算签名
        headers.put("Authorization", "SDK-HMAC-SHA256" + " " + "Access=" + getAccessKey()
                + ", " + "SignedHeaders=" + SIGNEDHEADERS + ", " + "Signature=" + signature);

        // 2. 发起请求
        String responseBody = HttpUtils.post(URL, headers, requestBody);
        return JsonUtils.parseTree(responseBody);
    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String requestBody) {
        Map<String, String> params = decodeParamMap(requestBody);
        // 字段参考 https://support.huaweicloud.com/api-msgsms/sms_05_0003.html
        return List.of(new SmsReceiveRespDTO()
                .setSuccess("DELIVRD".equals(params.get("status"))) // 是否接收成功
                .setErrorCode(params.get("status")) // 状态报告编码
                .setErrorMsg(params.get("statusDesc"))
                .setMobile(params.get("to")) // 手机号
                .setReceiveTime(LocalDateTime.ofInstant(Instant.parse(params.get("updateTime")), ZoneId.of("UTC"))) // 状态报告时间
                .setSerialNo(params.get("smsMsgId")) // 发送序列号
                .setLogId(Long.valueOf(params.get("extend")))); // 用户序列号
    }

    /**
     * 解析 URL 查询参数字符串为 Map，等价于 hutool 的 HttpUtil.decodeParamMap
     */
    private static Map<String, String> decodeParamMap(String paramString) {
        Map<String, String> params = new HashMap<>();
        if (paramString == null || paramString.isEmpty()) {
            return params;
        }
        for (String pair : paramString.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int index = pair.indexOf('=');
            String key = index > 0 ? pair.substring(0, index) : pair;
            String value = index > 0 ? pair.substring(index + 1) : "";
            try {
                params.put(URLDecoder.decode(key, StandardCharsets.UTF_8.name()),
                        URLDecoder.decode(value, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("解码回调参数失败", e);
            }
        }
        return params;
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) throws Throwable {
        // 华为短信模板查询和发送短信，是不同的两套 key 和 secret，与阿里、腾讯的区别较大，这里模板查询校验暂不实现
        String[] strs = apiTemplateId.split(" ");
        if (strs.length != 2) {
            throw new IllegalArgumentException("格式不正确，需要满足：apiTemplateId sender");
        }
        return new SmsTemplateRespDTO().setId(apiTemplateId).setContent(null)
                .setAuditStatus(SmsTemplateAuditStatusEnum.SUCCESS.getStatus()).setAuditReason(null);
    }

    private static void appendToBody(StringBuilder body, String key, String value) {
        if (StringUtils.isNotEmpty(value)) {
            body.append(key).append(HttpUtils.encodeUtf8(value));
        }
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

}
