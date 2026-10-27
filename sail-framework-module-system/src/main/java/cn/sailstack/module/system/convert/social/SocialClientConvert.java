package cn.sailstack.module.system.convert.social;

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.system.api.social.dto.SocialWxJsapiSignatureRespDTO;
import cn.sailstack.module.system.api.social.dto.SocialWxPhoneNumberInfoRespDTO;
import cn.sailstack.module.system.api.social.dto.SocialWxaSubscribeTemplateRespDTO;
import cn.sailstack.module.system.controller.admin.social.vo.client.SocialClientRespVO;
import cn.sailstack.module.system.controller.admin.social.vo.client.SocialClientSaveReqVO;
import cn.sailstack.module.system.dal.dataobject.social.SocialClientDO;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.subscribemsg.TemplateInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SocialClientConvert {

    SocialClientConvert INSTANCE = Mappers.getMapper(SocialClientConvert.class);

    SocialWxJsapiSignatureRespDTO convert(WxJsapiSignature bean);

    SocialWxPhoneNumberInfoRespDTO convert(WxMaPhoneNumberInfo bean);

    SocialWxaSubscribeTemplateRespDTO convert(TemplateInfo bean);

    SocialClientDO convertToDO(SocialClientSaveReqVO bean);

    SocialClientRespVO convertToRespVO(SocialClientDO bean);

    List<SocialClientRespVO> convertRespVOList(List<SocialClientDO> list);

    default PageResult<SocialClientRespVO> convertRespVOPage(PageResult<SocialClientDO> page) {
        return new PageResult<>(convertRespVOList(page.getList()), page.getTotal());
    }

}
