package cn.sailstack.module.system.convert.oauth2;

import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.system.controller.admin.oauth2.vo.client.OAuth2ClientRespVO;
import cn.sailstack.module.system.controller.admin.oauth2.vo.client.OAuth2ClientSaveReqVO;
import cn.sailstack.module.system.controller.admin.oauth2.vo.token.OAuth2AccessTokenRespVO;
import cn.sailstack.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.sailstack.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface OAuth2ClientConvert {

    OAuth2ClientConvert INSTANCE = Mappers.getMapper(OAuth2ClientConvert.class);

    OAuth2ClientDO convertToDO(OAuth2ClientSaveReqVO bean);

    OAuth2ClientRespVO convertToRespVO(OAuth2ClientDO bean);

    List<OAuth2ClientRespVO> convertRespVOList(List<OAuth2ClientDO> list);

    default PageResult<OAuth2ClientRespVO> convertRespVOPage(PageResult<OAuth2ClientDO> page) {
        return new PageResult<>(convertRespVOList(page.getList()), page.getTotal());
    }

    OAuth2AccessTokenRespVO convertToAccessTokenRespVO(OAuth2AccessTokenDO bean);

    List<OAuth2AccessTokenRespVO> convertAccessTokenRespVOList(List<OAuth2AccessTokenDO> list);

    default PageResult<OAuth2AccessTokenRespVO> convertAccessTokenRespVOPage(PageResult<OAuth2AccessTokenDO> page) {
        return new PageResult<>(convertAccessTokenRespVOList(page.getList()), page.getTotal());
    }

}
