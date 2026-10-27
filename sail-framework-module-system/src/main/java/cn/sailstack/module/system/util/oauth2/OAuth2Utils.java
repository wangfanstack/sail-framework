package cn.sailstack.module.system.util.oauth2;

import cn.sailstack.framework.common.util.http.HttpUtils;
import cn.sailstack.framework.security.core.util.SecurityFrameworkUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OAuth2 相关的工具类
 *
 * @author 农E链源码
 */
public class OAuth2Utils {

    /**
     * 构建授权码模式下，重定向的 URI
     *
     * copy from Spring Security OAuth2 的 AuthorizationEndpoint 类的 getSuccessfulRedirect 方法
     *
     * @param redirectUri 重定向 URI
     * @param authorizationCode 授权码
     * @param state 状态
     * @return 授权码模式下的重定向 URI
     */
    public static String buildAuthorizationCodeRedirectUri(String redirectUri, String authorizationCode, String state) {
        Map<String, String> query = new LinkedHashMap<>();
        query.put("code", authorizationCode);
        if (state != null) {
            query.put("state", state);
        }
        return HttpUtils.append(redirectUri, query, null, false);
    }

    /**
     * 构建简化模式下，重定向的 URI
     *
     * copy from Spring Security OAuth2 的 AuthorizationEndpoint 类的 appendAccessToken 方法
     *
     * @param redirectUri 重定向 URI
     * @param accessToken 访问令牌
     * @param state 状态
     * @param expireTime 过期时间
     * @param scopes 授权范围
     * @param additionalInformation 附加信息
     * @return 简化授权模式下的重定向 URI
     */
    public static String buildImplicitRedirectUri(String redirectUri, String accessToken, String state, LocalDateTime expireTime,
                                                  Collection<String> scopes, Map<String, Object> additionalInformation) {
        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        Map<String, String> keys = new HashMap<String, String>();
        vars.put("access_token", accessToken);
        vars.put("token_type", SecurityFrameworkUtils.AUTHORIZATION_BEARER.toLowerCase());
        if (state != null) {
            vars.put("state", state);
        }
        if (expireTime != null) {
            vars.put("expires_in", getExpiresIn(expireTime));
        }
        if (scopes != null && !scopes.isEmpty()) {
            vars.put("scope", buildScopeStr(scopes));
        }
        if (additionalInformation != null && !additionalInformation.isEmpty()) {
            for (String key : additionalInformation.keySet()) {
                Object value = additionalInformation.get(key);
                if (value != null) {
                    keys.put("extra_" + key, key);
                    vars.put("extra_" + key, value);
                }
            }
        }
        // Do not include the refresh token (even if there is one)
        return HttpUtils.append(redirectUri, vars, keys, true);
    }

    public static String buildUnsuccessfulRedirect(String redirectUri, String responseType, String state,
                                                   String error, String description) {
        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put("error", error);
        query.put("error_description", description);
        if (state != null) {
            query.put("state", state);
        }
        return HttpUtils.append(redirectUri, query, null, !responseType.contains("code"));
    }

    public static long getExpiresIn(LocalDateTime expireTime) {
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), expireTime);
    }

    public static String buildScopeStr(Collection<String> scopes) {
        return scopes.stream().collect(Collectors.joining(" "));
    }

    public static List<String> buildScopes(String scope) {
        return StringUtils.isBlank(scope) ? new ArrayList<>()
                : Arrays.stream(scope.split(" ")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

}
