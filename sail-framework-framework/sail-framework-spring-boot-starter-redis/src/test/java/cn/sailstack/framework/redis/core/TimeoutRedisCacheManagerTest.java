package cn.sailstack.framework.redis.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link TimeoutRedisCacheManager} 的单元测试
 *
 * @author 农E链源码
 */
public class TimeoutRedisCacheManagerTest {

    private TimeoutRedisCacheManager cacheManager;

    @BeforeEach
    public void setUp() {
        cacheManager = new TimeoutRedisCacheManager(Mockito.mock(RedisCacheWriter.class),
                RedisCacheConfiguration.defaultCacheConfig());
    }

    @Test
    public void testCreateRedisCache_withSecondTtl() {
        RedisCache cache = cacheManager.createRedisCache("demo#30s", RedisCacheConfiguration.defaultCacheConfig());
        assertEquals("demo", cache.getName());
        assertEquals(Duration.ofSeconds(30), cache.getCacheConfiguration().getTtl());
    }

    @Test
    public void testCreateRedisCache_withMinuteTtl() {
        RedisCache cache = cacheManager.createRedisCache("demo#10m", RedisCacheConfiguration.defaultCacheConfig());
        assertEquals("demo", cache.getName());
        assertEquals(Duration.ofMinutes(10), cache.getCacheConfiguration().getTtl());
    }

    @Test
    public void testCreateRedisCache_withHourTtl() {
        RedisCache cache = cacheManager.createRedisCache("demo#1h", RedisCacheConfiguration.defaultCacheConfig());
        assertEquals("demo", cache.getName());
        assertEquals(Duration.ofHours(1), cache.getCacheConfiguration().getTtl());
    }

    @Test
    public void testCreateRedisCache_withDayTtl() {
        RedisCache cache = cacheManager.createRedisCache("demo#2d", RedisCacheConfiguration.defaultCacheConfig());
        assertEquals("demo", cache.getName());
        assertEquals(Duration.ofDays(2), cache.getCacheConfiguration().getTtl());
    }

    @Test
    public void testCreateRedisCache_withoutUnit_shouldUseSeconds() {
        RedisCache cache = cacheManager.createRedisCache("demo#60", RedisCacheConfiguration.defaultCacheConfig());
        assertEquals("demo", cache.getName());
        assertEquals(Duration.ofSeconds(60), cache.getCacheConfiguration().getTtl());
    }

    @Test
    public void testCreateRedisCache_withoutTtl_shouldKeepOriginName() {
        RedisCache cache = cacheManager.createRedisCache("demo", RedisCacheConfiguration.defaultCacheConfig());
        assertEquals("demo", cache.getName());
    }

}
