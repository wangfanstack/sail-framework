package cn.sailstack.module.system.convert.oauth2;

import cn.sailstack.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import cn.sailstack.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import cn.sailstack.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.sailstack.module.system.dal.dataobject.oauth2.OAuth2RefreshTokenDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OAuth2TokenConvert {

    OAuth2TokenConvert INSTANCE = Mappers.getMapper(OAuth2TokenConvert.class);

    OAuth2AccessTokenRespDTO convertToRespDTO(OAuth2AccessTokenDO bean);

    OAuth2AccessTokenCheckRespDTO convertToCheckRespDTO(OAuth2AccessTokenDO bean);

    OAuth2AccessTokenDO convertToAccessTokenDO(OAuth2RefreshTokenDO bean);

}
