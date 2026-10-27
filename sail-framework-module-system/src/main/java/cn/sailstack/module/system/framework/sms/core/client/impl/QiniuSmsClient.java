package cn.sailstack.module.system.framework.sms.core.client.impl;

import cn.sailstack.framework.common.core.KeyValue;
import cn.sailstack.framework.common.util.date.LocalDateTimeUtils;
import cn.sailstack.framework.common.util.http.HttpUtils;
import cn.sailstack.framework.common.util.json.JsonUtils;
import cn.sailstack.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.sailstack.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.sailstack.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.sailstack.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.sailstack.module.system.framework.sms.core.property.SmsChannelProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.sailstack.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 七牛云短信客户端的实现类
 *
 * @author scholar
 * @since 2024/08/26 15:35
 */
@Slf4j
public class QiniuSmsClient extends AbstractSmsClient {

    private static final String HOST = "sms.qiniuapi.com";

    private static final DateTimeFormatter SIGN_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneId.of("UTC"));

    public QiniuSmsClient(SmsChannelProperties properties) {
        super(properties);
        if (properties.getApiKey() == null || properties.getApiKey().isEmpty()) {
            throw new IllegalArgumentException("apiKey 不能为空");
        }
        if (properties.getApiSecret() == null || properties.getApiSecret().isEmpty()) {
            throw new IllegalArgumentException("apiSecret 不能为空");
        }
    }

    public SmsSendRespDTO sendSms(Long sendLogId, String mobile, String apiTemplateId,
                                  List<KeyValue<String, Object>> templateParams) throws Throwable {
        // 1. 执行请求
        // 参考链接 https://developer.qiniu.com/sms/5824/through-the-api-send-text-messages
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("template_id", apiTemplateId);
        body.put("mobile", mobile);
        body.put("parameters", templateParams.stream().collect(
                Collectors.toMap(KeyValue::getKey, KeyValue::getValue, (a, b) -> b, LinkedHashMap::new)));
        body.put("seq", Long.toString(sendLogId));
        JsonNode response = request("POST", body, "/v1/message/single");

        // 2. 解析请求
        if (JsonUtils.getText(response, "error") != null && !JsonUtils.getText(response, "error").isEmpty()) {
            // 短信请求失败
            return new SmsSendRespDTO().setSuccess(false)
                    .setApiCode(JsonUtils.getText(response, "error"))
                    .setApiRequestId(JsonUtils.getText(response, "request_id"))
                    .setApiMsg(JsonUtils.getText(response, "message"));
        }
        return new SmsSendRespDTO().setSuccess(response.has("message_id"))
                .setSerialNo(JsonUtils.getText(response, "message_id"));
    }

    /**
     * 请求七牛云短信
     *
     * @see <a href="https://developer.qiniu.com/sms/5842/sms-api-authentication"</>
     * @param httpMethod http请求方法
     * @param body http请求消息体
     * @param path URL path
     * @return 请求结果
     */
    private JsonNode request(String httpMethod, LinkedHashMap<String, Object> body, String path) {
        String signDate = SIGN_DATE_FORMATTER.format(Instant.now());
        // 1. 请求头
        Map<String, String> header = new HashMap<>(4);
        header.put("HOST", HOST);
        header.put("Authorization", getSignature(httpMethod, path, body != null ? JsonUtils.toJsonString(body) : "", signDate));
        header.put("Content-Type", "application/json");
        header.put("X-Qiniu-Date", signDate);

        // 2. 发起请求
        String responseBody;
        if (Objects.equals(httpMethod, "POST")){
            responseBody = HttpUtils.post("https://" + HOST + path, header, JsonUtils.toJsonString(body));
        } else {
            responseBody = HttpUtils.get("https://" + HOST + path, header);
        }
        return JsonUtils.parseTree(responseBody);
    }

    private String getSignature(String method, String path, String body, String signDate) {
        StringBuilder dataToSign = new StringBuilder();
        dataToSign.append(method.toUpperCase()).append(" ").append(path)
                .append("\nHost: ").append(HOST)
                .append("\n").append("Content-Type").append(": ").append("application/json")
                .append("\n").append("X-Qiniu-Date").append(": ").append(signDate)
                .append("\n\n");
        if (body != null && !body.isEmpty()) {
            dataToSign.append(body);
        }
        String signature = hmacSha1Base64(properties.getApiSecret(), dataToSign.toString());
        return "Qiniu " + properties.getApiKey() + ":" + signature;
    }

    private static String hmacSha1Base64(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalArgumentException("计算 HmacSHA1 签名失败", e);
        }
    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        JsonNode status = JsonUtils.parseTree(text);
        // 字段参考 https://developer.qiniu.com/sms/5910/message-push
        List<JsonNode> items = new ArrayList<>();
        JsonNode itemsNode = status.get("items");
        if (itemsNode instanceof ArrayNode arrayNode) {
            arrayNode.forEach(items::add);
        }
        return convertList(items, statusObj -> new SmsReceiveRespDTO()
                .setSuccess("DELIVRD".equals(JsonUtils.getText(statusObj, "status"))) // 是否接收成功
                .setErrorMsg(JsonUtils.getText(statusObj, "status")) // 状态报告编码
                .setMobile(JsonUtils.getText(statusObj, "mobile")) // 手机号
                .setReceiveTime(LocalDateTimeUtils.ofEpochSecond(AliyunSmsClient.getLong(statusObj, "delivrd_at"))) // 状态报告时间
                .setSerialNo(JsonUtils.getText(statusObj, "message_id")) // 发送序列号
                .setLogId(AliyunSmsClient.getLong(statusObj, "seq"))); // 用户序列号
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) throws Throwable {
        // 1. 执行请求
        // 参考链接 https://developer.qiniu.com/sms/5969/query-a-single-template
        JsonNode response = request("GET", null, "/v1/template/" + apiTemplateId);

        // 2.2 解析请求
        return new SmsTemplateRespDTO()
                .setId(JsonUtils.getText(response, "id"))
                .setContent(JsonUtils.getText(response, "template"))
                .setAuditStatus(convertSmsTemplateAuditStatus(JsonUtils.getText(response, "audit_status")))
                .setAuditReason(JsonUtils.getText(response, "reject_reason"));
    }

    @VisibleForTesting
    Integer convertSmsTemplateAuditStatus(String templateStatus) {
        switch (templateStatus) {
            case "passed": return SmsTemplateAuditStatusEnum.SUCCESS.getStatus();
            case "reviewing": return SmsTemplateAuditStatusEnum.CHECKING.getStatus();
            case "rejected": return SmsTemplateAuditStatusEnum.FAIL.getStatus();
            default:
                throw new IllegalArgumentException(String.format("未知审核状态(%str)", templateStatus));
        }
    }
}
