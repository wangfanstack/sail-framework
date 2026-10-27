package cn.sailstack.framework.common.util.monitor;

import org.slf4j.MDC;

/**
 * 链路追踪工具类
 *
 * 基于 SLF4J MDC 实现：Micrometer Tracing（Brave Bridge）在 Span 作用域内会自动写入
 * traceId / spanId（由 Spring Boot 的 BraveAutoConfiguration 提供的 MDCScopeDecorator 完成）。
 *
 * 考虑到每个 starter 都需要用到该工具类，所以放到 common 模块下的 util 包下
 *
 * @author 农E链源码
 */
public class TracerUtils {

    /**
     * MDC 属性名 - 链路追踪编号（与 Micrometer Tracing Brave Bridge 的默认 key 保持一致）
     */
    public static final String MDC_TRACE_ID = "traceId";

    /**
     * 私有化构造方法
     */
    private TracerUtils() {
    }

    /**
     * 获得链路追踪编号，直接返回当前 Span 的 TraceId。
     * 如果不存在的话为空字符串！！！
     *
     * @return 链路追踪编号
     */
    public static String getTraceId() {
        String traceId = MDC.get(MDC_TRACE_ID);
        return traceId != null ? traceId : "";
    }

}
