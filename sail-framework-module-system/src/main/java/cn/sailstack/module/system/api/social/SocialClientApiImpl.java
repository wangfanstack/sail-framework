package cn.sailstack.module.system.api.social;

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.sailstack.module.system.api.social.dto.*;
import cn.sailstack.module.system.convert.social.SocialClientConvert;
import cn.sailstack.module.system.enums.social.SocialTypeEnum;
import cn.sailstack.module.system.service.social.SocialClientService;
import cn.sailstack.module.system.service.social.SocialUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.subscribemsg.TemplateInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static cn.sailstack.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 社交应用的 API 实现类
 *
 * @author 农E链源码
 */
@Service
@Validated
@Slf4j
public class SocialClientApiImpl implements SocialClientApi {

    @Resource
    private SocialClientService socialClientService;
    @Resource
    private SocialUserService socialUserService;

    @Override
    public String getAuthorizeUrl(Integer socialType, Integer userType, String redirectUri) {
        return socialClientService.getAuthorizeUrl(socialType, userType, redirectUri);
    }

    @Override
    public SocialWxJsapiSignatureRespDTO createWxMpJsapiSignature(Integer userType, String url) {
        WxJsapiSignature signature = socialClientService.createWxMpJsapiSignature(userType, url);
        return SocialClientConvert.INSTANCE.convert(signature);
    }

    //======================= 微信小程序独有 =======================

    @Override
    public SocialWxPhoneNumberInfoRespDTO getWxMaPhoneNumberInfo(Integer userType, String phoneCode) {
        WxMaPhoneNumberInfo info = socialClientService.getWxMaPhoneNumberInfo(userType, phoneCode);
        return SocialClientConvert.INSTANCE.convert(info);
    }

    @Override
    public byte[] getWxaQrcode(SocialWxQrcodeReqDTO reqVO) {
        return socialClientService.getWxaQrcode(reqVO);
    }

    @Override
    public List<SocialWxaSubscribeTemplateRespDTO> getWxaSubscribeTemplateList(Integer userType) {
        List<TemplateInfo> list = socialClientService.getSubscribeTemplateList(userType);
        return convertList(list, item -> SocialClientConvert.INSTANCE.convert(item).setId(item.getPriTmplId()));
    }

    @Override
    public void sendWxaSubscribeMessage(SocialWxaSubscribeMessageSendReqDTO reqDTO) {
        // 1.1 获得订阅模版列表
        List<TemplateInfo> templateList = socialClientService.getSubscribeTemplateList(reqDTO.getUserType());
        if (templateList == null || templateList.isEmpty()) {
            log.warn("[sendSubscribeMessage][reqDTO({}) 发送订阅消息失败，原因：没有找到订阅模板]", reqDTO);
            return;
        }
        // 1.2 获得需要使用的模版
        TemplateInfo template = templateList.stream().filter(item ->
                Objects.equals(item.getTitle(), reqDTO.getTemplateTitle())).findFirst().orElse(null);
        if (template == null) {
            log.warn("[sendWxaSubscribeMessage][reqDTO({}) 发送订阅消息失败，原因：没有找到订阅模板]", reqDTO);
            return;
        }

        // 2. 获得社交用户
        SocialUserRespDTO socialUser = socialUserService.getSocialUserByUserId(reqDTO.getUserType(), reqDTO.getUserId(),
                SocialTypeEnum.WECHAT_MINI_PROGRAM.getType());
        if (socialUser == null || StringUtils.isBlank(socialUser.getOpenid())) {
            log.warn("[sendWxaSubscribeMessage][reqDTO({}) 发送订阅消息失败，原因：会员 openid 缺失]", reqDTO);
            return;
        }

        // 3. 发送订阅消息
        socialClientService.sendSubscribeMessage(reqDTO, template.getPriTmplId(), socialUser.getOpenid());
    }

    @Override
    public void uploadWxaOrderShippingInfo(Integer userType, SocialWxaOrderUploadShippingInfoReqDTO reqDTO) {
        socialClientService.uploadWxaOrderShippingInfo(userType, reqDTO);
    }

    @Override
    public void notifyWxaOrderConfirmReceive(Integer userType, SocialWxaOrderNotifyConfirmReceiveReqDTO reqDTO) {
        socialClientService.notifyWxaOrderConfirmReceive(userType, reqDTO);
    }

}
