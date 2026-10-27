package cn.sailstack.framework.encrypt.core.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 解密请求 {@link HttpServletRequestWrapper} 实现类
 *
 * @author 农E链源码
 */
public class ApiDecryptRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public ApiDecryptRequestWrapper(HttpServletRequest request, ApiEncryptCipher decryptCipher) throws IOException {
        super(request);
        // 读取 body（Base64 传输）
        String requestBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // 解密 body
        body = decryptCipher.decrypt(requestBody).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    @Override
    public int getContentLength() {
        return body.length;
    }

    @Override
    public long getContentLengthLong() {
        return body.length;
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream stream = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public int read() {
                return stream.read();
            }

            @Override
            public int available() {
                return body.length;
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

        };
    }
}
