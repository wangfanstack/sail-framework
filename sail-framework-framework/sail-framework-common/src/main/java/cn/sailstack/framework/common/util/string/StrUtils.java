package cn.sailstack.framework.common.util.string;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 *
 * @author 农E链源码
 */
public class StrUtils {

    public static String maxLength(CharSequence str, int maxLength) {
        // 与原 hutool 语义一致：超出部分以 ... 结尾
        return StringUtils.abbreviate(str == null ? null : str.toString(), maxLength);
    }

    /**
     * 给定字符串是否以任何一个字符串开始
     * 给定字符串和数组为空都返回 false
     *
     * @param str      给定字符串
     * @param prefixes 需要检测的开始字符串
     */
    public static boolean startWithAny(String str, Collection<String> prefixes) {
        if (StringUtils.isEmpty(str) || prefixes == null || prefixes.isEmpty()) {
            return false;
        }
        for (String prefix : prefixes) {
            if (StringUtils.startsWith(str, prefix)) {
                return true;
            }
        }
        return false;
    }

    public static List<Long> splitToLong(String value, CharSequence separator) {
        return splitToArray(value, separator).stream().map(Long::valueOf).collect(Collectors.toList());
    }

    public static Set<Long> splitToLongSet(String value) {
        return splitToLongSet(value, ",");
    }

    public static Set<Long> splitToLongSet(String value, CharSequence separator) {
        return splitToArray(value, separator).stream().map(Long::valueOf).collect(Collectors.toSet());
    }

    public static List<Integer> splitToInteger(String value, CharSequence separator) {
        return splitToArray(value, separator).stream().map(Integer::valueOf).collect(Collectors.toList());
    }

    private static List<String> splitToArray(String value, CharSequence separator) {
        if (StringUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        String sep = separator.toString();
        return Arrays.stream(StringUtils.split(value, sep))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    /**
     * 移除字符串中，包含指定字符串的行
     *
     * @param content 字符串
     * @param sequence 包含的字符串
     * @return 移除后的字符串
     */
    public static String removeLineContains(String content, String sequence) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(sequence)) {
            return content;
        }
        return Arrays.stream(content.split("\n"))
                .filter(line -> !line.contains(sequence))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 拼接方法的参数
     *
     * 特殊：排除一些无法序列化的参数，如 ServletRequest、ServletResponse、MultipartFile
     *
     * @param joinPoint 连接点
     * @return 拼接后的参数
     */
    public static String joinMethodArgs(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args)
                .map(item -> {
                    if (item == null) {
                        return "";
                    }
                    // 讨论可见：https://t.zsxq.com/XUJVk、https://t.zsxq.com/MnKcL
                    String clazzName = item.getClass().getName();
                    if (StringUtils.startsWithAny(clazzName, "javax.servlet", "jakarta.servlet", "org.springframework.web")) {
                        return "";
                    }
                    return String.valueOf(item);
                })
                .collect(Collectors.joining(","));
    }

}
