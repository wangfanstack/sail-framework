package cn.sailstack.framework.encrypt.core.filter;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * API 加解密工具，基于 JDK JCA 实现（替代 hutool-crypto）
 *
 * AES：ECB/PKCS5Padding，密钥为 UTF-8 字符串（16/24/32 位）
 * RSA：ECB/PKCS5Padding，密钥为 Base64 编码的 X.509 公钥 / PKCS#8 私钥
 *
 * 报文统一使用 Base64 编码传输
 *
 * @author 农E链源码
 */
public final class ApiEncryptCipher {

    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS5Padding";
    private static final String RSA_ALGORITHM = "RSA";

    private final boolean symmetric;
    private final SecretKeySpec aesKey;
    private final PrivateKey rsaPrivateKey;
    private final PublicKey rsaPublicKey;

    private ApiEncryptCipher(SecretKeySpec aesKey) {
        this.symmetric = true;
        this.aesKey = aesKey;
        this.rsaPrivateKey = null;
        this.rsaPublicKey = null;
    }

    private ApiEncryptCipher(PrivateKey rsaPrivateKey, PublicKey rsaPublicKey) {
        this.symmetric = false;
        this.aesKey = null;
        this.rsaPrivateKey = rsaPrivateKey;
        this.rsaPublicKey = rsaPublicKey;
    }

    /**
     * 构建 AES 加解密器
     *
     * @param key 密钥字符串（16/24/32 位）
     */
    public static ApiEncryptCipher aes(String key) {
        return new ApiEncryptCipher(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"));
    }

    /**
     * 构建 RSA 加解密器（私钥，用于解密请求）
     *
     * @param privateKeyBase64 Base64 编码的 PKCS#8 私钥
     */
    public static ApiEncryptCipher rsaPrivate(String privateKeyBase64) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64)));
            return new ApiEncryptCipher(privateKey, null);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("RSA 私钥解析失败", e);
        }
    }

    /**
     * 构建 RSA 加解密器（公钥，用于加密响应）
     *
     * @param publicKeyBase64 Base64 编码的 X.509 公钥
     */
    public static ApiEncryptCipher rsaPublic(String publicKeyBase64) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64)));
            return new ApiEncryptCipher(null, publicKey);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("RSA 公钥解析失败", e);
        }
    }

    /**
     * 解密 Base64 报文，返回明文字符串
     *
     * @param base64Body Base64 编码的密文
     * @return 明文
     */
    public String decrypt(String base64Body) {
        return new String(decryptBytes(Base64.getDecoder().decode(base64Body)), StandardCharsets.UTF_8);
    }

    private byte[] decryptBytes(byte[] cipherBytes) {
        try {
            Cipher cipher = Cipher.getInstance(symmetric ? AES_TRANSFORMATION : RSA_TRANSFORMATION);
            if (symmetric) {
                cipher.init(Cipher.DECRYPT_MODE, aesKey);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
            }
            return cipher.doFinal(cipherBytes);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("解密失败", e);
        }
    }

    /**
     * 加密并输出 Base64 报文
     *
     * @param body 明文字符串
     * @return Base64 编码的密文
     */
    public String encryptBase64(String body) {
        return encryptBase64(body.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 加密并输出 Base64 报文
     *
     * @param body 明文字节
     * @return Base64 编码的密文
     */
    public String encryptBase64(byte[] body) {
        try {
            Cipher cipher = Cipher.getInstance(symmetric ? AES_TRANSFORMATION : RSA_TRANSFORMATION);
            if (symmetric) {
                cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
            }
            return Base64.getEncoder().encodeToString(cipher.doFinal(body));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("加密失败", e);
        }
    }

}
