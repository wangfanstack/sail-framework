package cn.sailstack.framework.common.util.monitor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link TracerUtils} 的单元测试
 *
 * @author 农E链源码
 */
public class TracerUtilsTest {

    @AfterEach
    public void tearDown() {
        MDC.remove(TracerUtils.MDC_TRACE_ID);
    }

    @Test
    public void testGetTraceId_whenMdcPresent() {
        MDC.put(TracerUtils.MDC_TRACE_ID, "0af7651916cd43dd8448eb211c80319c");
        assertEquals("0af7651916cd43dd8448eb211c80319c", TracerUtils.getTraceId());
    }

    @Test
    public void testGetTraceId_whenMdcAbsent_shouldReturnEmptyString() {
        assertEquals("", TracerUtils.getTraceId());
    }

}
