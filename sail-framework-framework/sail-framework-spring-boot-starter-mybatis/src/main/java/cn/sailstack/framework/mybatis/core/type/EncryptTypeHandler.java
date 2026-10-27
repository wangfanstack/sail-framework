package cn.sailstack.framework.mybatis.core.type;

import cn.sailstack.framework.common.util.spring.SpringUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * 字段加密的 TypeHandler 实现类，基于 JDK AES/ECB/PKCS5Padding 实现
 * 可通过 mybatis-plus.encryptor.password 配置项，设置密钥
 *
 * @author 农E链源码
 */
public class EncryptTypeHandler extends BaseTypeHandler<String> {

    private static final String ENCRYPTOR_PROPERTY_NAME = "mybatis-plus.encryptor.password";

    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private static volatile SecretKeySpec keySpec;

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return decrypt(value);
    }

    private static String decrypt(String value) {
        if (value == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getEncryptor());
            return new String(cipher.doFinal(Base64.getDecoder().decode(value)), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("解密失败", e);
        }
    }

    public static String encrypt(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getEncryptor());
            return Base64.getEncoder().encodeToString(cipher.doFinal(rawValue.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("加密失败", e);
        }
    }

    private static SecretKeySpec getEncryptor() {
        if (keySpec != null) {
            return keySpec;
        }
        // 构建 AES 密钥
        String password = SpringUtils.getProperty(ENCRYPTOR_PROPERTY_NAME);
        if (StringUtils.isEmpty(password)) {
            throw new IllegalStateException(String.format("配置项(%s) 不能为空", ENCRYPTOR_PROPERTY_NAME));
        }
        keySpec = new SecretKeySpec(password.getBytes(StandardCharsets.UTF_8), "AES");
        return keySpec;
    }

}
