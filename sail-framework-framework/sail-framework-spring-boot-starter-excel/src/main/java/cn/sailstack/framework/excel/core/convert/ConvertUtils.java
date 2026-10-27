package cn.sailstack.framework.excel.core.convert;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Excel 转换器专用的宽松类型转换工具
 *
 * 用于替代原 hutool {@code Convert.convert(Class, Object)} 在 {@link AreaConvert}、
 * {@link DictConvert}、{@link MultiDictConvert} 中的用法：目标类型是反射得到的字段类型，
 * 实际场景为 String / Integer / Long 等标量。
 *
 * 语义与 hutool 保持一致：
 * 1. value 为 null 时返回 null；
 * 2. 目标为 String 时转为字符串；
 * 3. 类型匹配时原样返回；
 * 4. 数值/布尔解析前 trim，空白字符串视为 null；
 * 5. 不支持的类型抛出 {@link IllegalArgumentException}（对应 hutool 的 ConvertException）。
 *
 * @author 农E链源码
 */
final class ConvertUtils {

    private ConvertUtils() {
    }

    /**
     * 将 value 宽松转换成 targetType 类型
     *
     * @param targetType 目标类型（通常是实体字段类型）
     * @param value      待转换的值（通常是 String 或 Number）
     * @return 转换后的值；value 为 null 或空白字符串时返回 null
     * @throws IllegalArgumentException 不支持的目标类型或解析失败时抛出
     */
    static Object convertValue(Class<?> targetType, Object value) {
        if (targetType == String.class) {
            return value == null ? null : value.toString();
        }
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }
        String str = value.toString().trim();
        // 空白字符串视为 null，与 hutool 的转换语义保持一致
        if (str.isEmpty()) {
            return null;
        }
        if (targetType == Integer.class || targetType == int.class) {
            return Integer.valueOf(str);
        }
        if (targetType == Long.class || targetType == long.class) {
            return Long.valueOf(str);
        }
        if (targetType == Double.class || targetType == double.class) {
            return Double.valueOf(str);
        }
        if (targetType == Float.class || targetType == float.class) {
            return Float.valueOf(str);
        }
        if (targetType == Short.class || targetType == short.class) {
            return Short.valueOf(str);
        }
        if (targetType == Byte.class || targetType == byte.class) {
            return Byte.valueOf(str);
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            if ("true".equalsIgnoreCase(str)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(str)) {
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException(String.format("无法将(%s)转换为 Boolean", str));
        }
        if (targetType == BigDecimal.class) {
            return new BigDecimal(str);
        }
        if (targetType == BigInteger.class) {
            return new BigInteger(str);
        }
        throw new IllegalArgumentException(String.format("不支持将(%s)转换为(%s)类型", str, targetType.getName()));
    }

}
