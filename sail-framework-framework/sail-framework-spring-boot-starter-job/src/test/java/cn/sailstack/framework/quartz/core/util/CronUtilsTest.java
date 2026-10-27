package cn.sailstack.framework.quartz.core.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CronUtils} 的单元测试
 *
 * @author 农E链源码
 */
public class CronUtilsTest {

    @Test
    public void testIsValid() {
        assertTrue(CronUtils.isValid("0 0 0 * * ?"));
        assertTrue(CronUtils.isValid("*/10 * * * * ?"));
        assertFalse(CronUtils.isValid("invalid"));
        assertFalse(CronUtils.isValid(""));
    }

    @Test
    public void testGetNextTimes() {
        List<LocalDateTime> nextTimes = CronUtils.getNextTimes("0 0 0 * * ?", 3);
        assertEquals(3, nextTimes.size());
        // 每个时间必须严格递增，且都晚于当前时间
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < nextTimes.size(); i++) {
            assertTrue(nextTimes.get(i).isAfter(now));
            if (i > 0) {
                assertTrue(nextTimes.get(i).isAfter(nextTimes.get(i - 1)));
            }
        }
    }

}
