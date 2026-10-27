package cn.sailstack.framework.common.util.number;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数字的工具类
 *
 * @author 农E链源码
 */
public class NumberUtils {

    private static final Pattern FIRST_BIG_DECIMAL_PATTERN = Pattern.compile("-?\\d+(?:\\.\\d+)?");

    /**
     * 一百，用于百分数和比例计算
     */
    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public static Long parseLong(String str) {
        return StringUtils.isNotEmpty(str) ? Long.valueOf(str) : null;
    }

    public static Integer parseInt(String str) {
        return StringUtils.isNotEmpty(str) ? Integer.valueOf(str) : null;
    }

    /**
     * 获得数字，为 null 时返回 {@link BigDecimal#ZERO}
     *
     * @param value 数字
     * @return 数字
     */
    public static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 从文本中解析首个数字
     *
     * @param text 文本
     * @return 数字；不存在时返回 {@link BigDecimal#ZERO}
     */
    public static BigDecimal parseFirstBigDecimal(String text) {
        if (StringUtils.isEmpty(text)) {
            return BigDecimal.ZERO;
        }
        Matcher matcher = FIRST_BIG_DECIMAL_PATTERN.matcher(text.replace(",", ""));
        if (!matcher.find()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(matcher.group());
    }

    public static boolean isAllNumber(List<String> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        for (String value : values) {
            if (!org.apache.commons.lang3.math.NumberUtils.isCreatable(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 通过经纬度获取地球上两点之间的距离
     *
     * @param lat1 经度1
     * @param lng1 纬度1
     * @param lat2 经度2
     * @param lng2 纬度2
     * @return 距离，单位：千米
     */
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = lat1 * Math.PI / 180.0;
        double radLat2 = lat2 * Math.PI / 180.0;
        double a = radLat1 - radLat2;
        double b = lng1 * Math.PI / 180.0 - lng2 * Math.PI / 180.0;
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        distance = distance * 6378.137;
        distance = Math.round(distance * 10000d) / 10000d;
        return distance;
    }

    /**
     * 提供精确的乘法运算
     *
     * 如果存在 null，则返回 null
     *
     * @param values 多个被乘值
     * @return 积
     */
    public static BigDecimal mul(BigDecimal... values) {
        BigDecimal result = null;
        for (BigDecimal value : values) {
            if (value == null) {
                return null;
            }
            result = result == null ? value : result.multiply(value);
        }
        return result;
    }

}
