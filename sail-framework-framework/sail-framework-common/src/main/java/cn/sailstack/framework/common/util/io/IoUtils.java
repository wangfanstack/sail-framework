package cn.sailstack.framework.common.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * IO 工具类
 *
 * @author 农E链源码
 */
public class IoUtils {

    /**
     * 从流中读取 UTF8 编码的内容
     *
     * @param in 输入流
     * @param isClose 是否关闭
     * @return 内容
     */
    public static String readUtf8(InputStream in, boolean isClose) {
        try {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (isClose) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

}
