package cn.sailstack.module.system.enums.oauth2;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth2 授权类型（模式）的枚举
 *
 * @author 农E链源码
 */
@AllArgsConstructor
@Getter
public enum OAuth2GrantTypeEnum {

    PASSWORD("password"), // 密码模式
    AUTHORIZATION_CODE("authorization_code"), // 授权码模式
    IMPLICIT("implicit"), // 简化模式
    CLIENT_CREDENTIALS("client_credentials"), // 客户端模式
    REFRESH_TOKEN("refresh_token"), // 刷新模式
    ;

    private final String grantType;

    public static OAuth2GrantTypeEnum getByGrantType(String grantType) {
        for (OAuth2GrantTypeEnum value : values()) {
            if (value.getGrantType().equals(grantType)) {
                return value;
            }
        }
        return null;
    }

}
