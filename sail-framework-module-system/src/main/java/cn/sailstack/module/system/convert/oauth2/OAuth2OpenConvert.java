package cn.sailstack.module.system.convert.oauth2;

import cn.sailstack.framework.common.core.KeyValue;
import cn.sailstack.framework.common.enums.UserTypeEnum;
import cn.sailstack.framework.common.util.collection.CollectionUtils;
import cn.sailstack.framework.security.core.util.SecurityFrameworkUtils;
import cn.sailstack.module.system.controller.admin.oauth2.vo.open.OAuth2OpenAccessTokenRespVO;
import cn.sailstack.module.system.controller.admin.oauth2.vo.open.OAuth2OpenAuthorizeInfoRespVO;
import cn.sailstack.module.system.controller.admin.oauth2.vo.open.OAuth2OpenCheckTokenRespVO;
import cn.sailstack.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.sailstack.module.system.dal.dataobject.oauth2.OAuth2ApproveDO;
import cn.sailstack.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import cn.sailstack.module.system.util.oauth2.OAuth2Utils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface OAuth2OpenConvert {

    OAuth2OpenConvert INSTANCE = Mappers.getMapper(OAuth2OpenConvert.class);

    OAuth2OpenAccessTokenRespVO convertToAccessTokenRespVO(OAuth2AccessTokenDO bean);

    OAuth2OpenCheckTokenRespVO convertToCheckTokenRespVO(OAuth2AccessTokenDO bean);

    default OAuth2OpenAccessTokenRespVO convert(OAuth2AccessTokenDO bean) {
        OAuth2OpenAccessTokenRespVO respVO = convertToAccessTokenRespVO(bean);
        respVO.setTokenType(SecurityFrameworkUtils.AUTHORIZATION_BEARER.toLowerCase());
        respVO.setExpiresIn(OAuth2Utils.getExpiresIn(bean.getExpiresTime()));
        respVO.setScope(OAuth2Utils.buildScopeStr(bean.getScopes()));
        return respVO;
    }

    default OAuth2OpenCheckTokenRespVO convert2(OAuth2AccessTokenDO bean) {
        OAuth2OpenCheckTokenRespVO respVO = convertToCheckTokenRespVO(bean);
        respVO.setExp(bean.getExpiresTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000L);
        respVO.setUserType(UserTypeEnum.ADMIN.getValue());
        return respVO;
    }

    default OAuth2OpenAuthorizeInfoRespVO convert(OAuth2ClientDO client, List<OAuth2ApproveDO> approves) {
        // 构建 scopes
        List<KeyValue<String, Boolean>> scopes = new ArrayList<>(client.getScopes().size());
        Map<String, OAuth2ApproveDO> approveMap = CollectionUtils.convertMap(approves, OAuth2ApproveDO::getScope);
        client.getScopes().forEach(scope -> {
            OAuth2ApproveDO approve = approveMap.get(scope);
            scopes.add(new KeyValue<>(scope, approve != null ? approve.getApproved() : false));
        });
        // 拼接返回
        return new OAuth2OpenAuthorizeInfoRespVO(
                new OAuth2OpenAuthorizeInfoRespVO.Client(client.getName(), client.getLogo()), scopes);
    }

}
