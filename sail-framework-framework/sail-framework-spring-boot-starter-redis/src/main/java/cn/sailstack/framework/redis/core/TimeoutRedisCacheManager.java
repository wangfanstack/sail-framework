package cn.sailstack.framework.redis.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;

/**
 * 支持自定义过期时间的 {@link RedisCacheManager} 实现类
 *
 * 在 {@link Cacheable#cacheNames()} 格式为 "key#ttl" 时，# 后面的 ttl 为过期时间。
 * 单位为最后一个字母（支持的单位有：d 天，h 小时，m 分钟，s 秒），默认单位为 s 秒
 *
 * @author 农E链源码
 */
public class TimeoutRedisCacheManager extends RedisCacheManager {

    private static final String SPLIT = "#";

    private static final String COLON = ":";

    public TimeoutRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        if (StringUtils.isEmpty(name)) {
            return super.createRedisCache(name, cacheConfig);
        }
        // 如果使用 # 分隔，大小不为 2，则说明不使用自定义过期时间
        String[] names = StringUtils.split(name, SPLIT);
        if (names == null || names.length != 2) {
            return super.createRedisCache(name, cacheConfig);
        }

        // 核心：通过修改 cacheConfig 的过期时间，实现自定义过期时间
        if (cacheConfig != null) {
            // 移除 # 后面的 : 以及后面的内容，避免影响解析
            String ttlStr = StringUtils.substringBefore(names[1], COLON); // 获得 ttlStr 时间部分
            names[1] = StringUtils.substringAfter(names[1], ttlStr); // 移除掉 ttlStr 时间部分
            // 解析时间
            Duration duration = parseDuration(ttlStr);
            cacheConfig = cacheConfig.entryTtl(duration);
        }

        // 创建 RedisCache 对象，需要忽略掉 ttlStr
        return super.createRedisCache(names[0] + names[1], cacheConfig);
    }

    /**
     * 解析过期时间 Duration
     *
     * @param ttlStr 过期时间字符串
     * @return 过期时间 Duration
     */
    private Duration parseDuration(String ttlStr) {
        char timeUnit = ttlStr.charAt(ttlStr.length() - 1);
        String amountStr = ttlStr.substring(0, ttlStr.length() - 1);
        switch (timeUnit) {
            case 'd':
                return Duration.ofDays(parseLong(amountStr));
            case 'h':
                return Duration.ofHours(parseLong(amountStr));
            case 'm':
                return Duration.ofMinutes(parseLong(amountStr));
            case 's':
                return Duration.ofSeconds(parseLong(amountStr));
            default:
                return Duration.ofSeconds(parseLong(ttlStr));
        }
    }

    private long parseLong(String str) {
        return NumberUtils.toLong(str, 0L);
    }

}
