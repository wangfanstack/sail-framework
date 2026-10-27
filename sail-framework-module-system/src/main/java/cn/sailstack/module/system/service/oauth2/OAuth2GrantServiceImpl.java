package cn.sailstack.module.system.service.oauth2;

import cn.sailstack.framework.common.enums.UserTypeEnum;
import cn.sailstack.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.sailstack.module.system.dal.dataobject.oauth2.OAuth2CodeDO;
import cn.sailstack.module.system.dal.dataobject.user.AdminUserDO;
import cn.sailstack.module.system.enums.ErrorCodeConstants;
import cn.sailstack.module.system.service.auth.AdminAuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static cn.sailstack.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * OAuth2 授予 Service 实现类
 *
 * @author 农E链源码
 */
@Service
public class OAuth2GrantServiceImpl implements OAuth2GrantService {

    @Resource
    private OAuth2TokenService oauth2TokenService;
    @Resource
    private OAuth2CodeService oauth2CodeService;
    @Resource
    private AdminAuthService adminAuthService;

    @Override
    public OAuth2AccessTokenDO grantImplicit(Long userId, Integer userType,
                                             String clientId, List<String> scopes) {
        return oauth2TokenService.createAccessToken(userId, userType, clientId, scopes);
    }

    @Override
    public String grantAuthorizationCodeForCode(Long userId, Integer userType,
                                                String clientId, List<String> scopes,
                                                String redirectUri, String state) {
        return oauth2CodeService.createAuthorizationCode(userId, userType, clientId, scopes,
                redirectUri, state).getCode();
    }

    @Override
    public OAuth2AccessTokenDO grantAuthorizationCodeForAccessToken(String clientId, String code,
                                                                    String redirectUri, String state) {
        OAuth2CodeDO codeDO = oauth2CodeService.consumeAuthorizationCode(code);
        if (codeDO == null) {
            throw new IllegalArgumentException("授权码不能为空"); // 防御性编程
        }
        // 校验 clientId 是否匹配
        if (!StringUtils.equals(clientId, codeDO.getClientId())) {
            throw exception(ErrorCodeConstants.OAUTH2_GRANT_CLIENT_ID_MISMATCH);
        }
        // 校验 redirectUri 是否匹配
        if (!StringUtils.equals(redirectUri, codeDO.getRedirectUri())) {
            throw exception(ErrorCodeConstants.OAUTH2_GRANT_REDIRECT_URI_MISMATCH);
        }
        // 校验 state 是否匹配
        state = StringUtils.defaultString(state); // 数据库 state 为 null 时，会设置为 "" 空串
        if (!StringUtils.equals(state, codeDO.getState())) {
            throw exception(ErrorCodeConstants.OAUTH2_GRANT_STATE_MISMATCH);
        }

        // 创建访问令牌
        return oauth2TokenService.createAccessToken(codeDO.getUserId(), codeDO.getUserType(),
                codeDO.getClientId(), codeDO.getScopes());
    }

    @Override
    public OAuth2AccessTokenDO grantPassword(String username, String password, String clientId, List<String> scopes) {
        // 使用账号 + 密码进行登录
        AdminUserDO user = adminAuthService.authenticate(username, password);
        if (user == null) {
            throw new IllegalArgumentException("用户不能为空！"); // 防御性编程
        }

        // 创建访问令牌
        return oauth2TokenService.createAccessToken(user.getId(), UserTypeEnum.ADMIN.getValue(), clientId, scopes);
    }

    @Override
    public OAuth2AccessTokenDO grantRefreshToken(String refreshToken, String clientId) {
        return oauth2TokenService.refreshAccessToken(refreshToken, clientId);
    }

    @Override
    public OAuth2AccessTokenDO grantClientCredentials(String clientId, List<String> scopes) {
        // 特殊：https://yuanbao.tencent.com/bot/app/share/chat/wFj642xSZHHx
        return oauth2TokenService.createAccessToken(0L, UserTypeEnum.ADMIN.getValue(), clientId, scopes);
    }

    @Override
    public boolean revokeToken(String clientId, String accessToken) {
        // 先查询，保证 clientId 时匹配的
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.getAccessToken(accessToken);
        if (accessTokenDO == null || !Objects.equals(clientId, accessTokenDO.getClientId())) {
            return false;
        }
        // 再删除
        return oauth2TokenService.removeAccessToken(accessToken) != null;
    }

}
