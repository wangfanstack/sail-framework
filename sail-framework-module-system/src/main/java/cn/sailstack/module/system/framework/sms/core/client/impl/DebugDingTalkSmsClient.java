package cn.sailstack.module.system.framework.sms.core.client.impl;

import cn.sailstack.framework.common.core.KeyValue;
import cn.sailstack.framework.common.util.collection.MapUtils;
import cn.sailstack.framework.common.util.http.HttpUtils;
import cn.sailstack.framework.common.util.json.JsonUtils;
import cn.sailstack.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.sailstack.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.sailstack.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.sailstack.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.sailstack.module.system.framework.sms.core.property.SmsChannelProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 基于钉钉 WebHook 实现的调试的短信客户端实现类
 *
 * 考虑到省钱，我们使用钉钉 WebHook 模拟发送短信，方便调试。
 *
 * @author 农E链源码
 */
public class DebugDingTalkSmsClient extends AbstractSmsClient {

    private static final String HMAC_SHA256 = "HmacSHA256";

    public DebugDingTalkSmsClient(SmsChannelProperties properties) {
        super(properties);
        if (properties.getApiKey() == null || properties.getApiKey().isEmpty()) {
            throw new IllegalArgumentException("apiKey 不能为空");
        }
        if (properties.getApiSecret() == null || properties.getApiSecret().isEmpty()) {
            throw new IllegalArgumentException("apiSecret 不能为空");
        }
    }

    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile,
                                  String apiTemplateId, List<KeyValue<String, Object>> templateParams) throws Throwable {
        // 构建请求
        String url = buildUrl("robot/send");
        Map<String, Object> params = new HashMap<>();
        params.put("msgtype", "text");
        String content = String.format("【模拟短信】\n手机号：%s\n短信日志编号：%d\n模板参数：%s",
                mobile, sendLogId, MapUtils.convertMap(templateParams));
        Map<String, Object> text = new HashMap<>();
        text.put("content", content);
        params.put("text", text);
        // 执行请求
        String responseText = HttpUtils.post(url,
                java.util.Collections.singletonMap("Content-Type", "application/json"), JsonUtils.toJsonString(params));
        // 解析结果
        Map<?, ?> responseObj = JsonUtils.parseObject(responseText, Map.class);
        String errorCode = (String) responseObj.get("errcode");
        return new SmsSendRespDTO().setSuccess(Objects.equals(errorCode, "0"))
                .setSerialNo(UUID.randomUUID().toString().replace("-", ""))
                .setApiCode(errorCode).setApiMsg((String) responseObj.get("errorMsg"));
    }

    /**
     * 构建请求地址
     *
     * 参见 <a href="https://developers.dingtalk.com/document/app/custom-robot-access/title-nfv-794-g71">文档</a>
     *
     * @param path 请求路径
     * @return 请求地址
     */
    @SuppressWarnings("SameParameterValue")
    private String buildUrl(String path) {
        // 生成 timestamp
        long timestamp = System.currentTimeMillis();
        // 生成 sign
        String secret = properties.getApiSecret();
        String stringToSign = timestamp + "\n" + secret;
        byte[] signData = hmacSha256(secret.getBytes(StandardCharsets.UTF_8), stringToSign);
        String sign = Base64.getEncoder().encodeToString(signData);
        // 构建最终 URL
        return String.format("https://oapi.dingtalk.com/%s?access_token=%s&timestamp=%d&sign=%s",
                path, properties.getApiKey(), timestamp, sign);
    }

    private static byte[] hmacSha256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(key, HMAC_SHA256));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("计算 HmacSHA256 签名失败", e);
        }
    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        throw new UnsupportedOperationException("模拟短信客户端，暂时无需解析回调");
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) {
        return new SmsTemplateRespDTO().setId(apiTemplateId).setContent("")
                .setAuditStatus(SmsTemplateAuditStatusEnum.SUCCESS.getStatus()).setAuditReason("");
    }

}
