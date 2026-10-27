package cn.sailstack.framework.common.util.date;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * 时间工具类，用于 {@link LocalDateTime}
 *
 * @author 农E链源码
 */
public class LocalDateTimeUtils {

    /**
     * 空的 LocalDateTime 对象，主要用于 DB 唯一索引的默认值
     */
    public static LocalDateTime EMPTY = buildTime(1970, 1, 1);

    /**
     * UTC 时间格式（毫秒 + 偏移），例如 2023-09-30T00:00:00.000+08:00
     */
    public static DateTimeFormatter UTC_MS_WITH_XXX_OFFSET_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    /**
     * 默认时区
     */
    private static final ZoneId DEFAULT_ZONE_ID = TimeZone.getTimeZone(DateUtils.TIME_ZONE_DEFAULT).toZoneId();

    /**
     * 将 Unix 秒时间戳转换为默认时区的本地时间
     *
     * @param epochSecond Unix 秒时间戳
     * @return 本地时间
     */
    public static LocalDateTime ofEpochSecond(long epochSecond) {
        return ofEpochSecond(epochSecond, DEFAULT_ZONE_ID);
    }

    /**
     * 将 Unix 秒时间戳转换为指定时区的本地时间
     *
     * @param epochSecond Unix 秒时间戳
     * @param zoneId 时区编号
     * @return 本地时间
     */
    public static LocalDateTime ofEpochSecond(long epochSecond, ZoneId zoneId) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), zoneId);
    }

    /**
     * 将给定的 {@link LocalDateTime} 转换为自 Unix 纪元时间（1970-01-01T00:00:00Z）以来的秒数
     *
     * @param sourceDateTime 需要转换的本地日期时间，不能为空
     * @return 自 1970-01-01T00:00:00Z 起的秒数（epoch second）
     */
    public static Long toEpochSecond(LocalDateTime sourceDateTime) {
        return toEpochSecond(sourceDateTime, DEFAULT_ZONE_ID);
    }

    /**
     * 将给定的 {@link LocalDateTime} 按指定时区转换为自 Unix 纪元时间（1970-01-01T00:00:00Z）以来的秒数
     *
     * @param sourceDateTime 需要转换的本地日期时间，不能为空
     * @param zoneId 时区编号
     * @return 自 1970-01-01T00:00:00Z 起的秒数（epoch second）
     */
    public static Long toEpochSecond(LocalDateTime sourceDateTime, ZoneId zoneId) {
        return sourceDateTime.atZone(zoneId).toEpochSecond();
    }

    /**
     * 创建指定时间
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return 指定时间
     */
    public static LocalDateTime buildTime(int year, int month, int day) {
        return LocalDateTime.of(year, month, day, 0, 0, 0);
    }

    /**
     * 解析时间字符串（yyyy-MM-dd 或 ISO 格式 yyyy-MM-ddTHH:mm:ss）
     *
     * @param time 时间字符串
     * @return 时间
     */
    public static LocalDateTime parse(String time) {
        if (time.length() <= 10) {
            return LocalDateTime.parse(time + "T00:00:00");
        }
        return LocalDateTime.parse(time.replace(' ', 'T'));
    }

}
