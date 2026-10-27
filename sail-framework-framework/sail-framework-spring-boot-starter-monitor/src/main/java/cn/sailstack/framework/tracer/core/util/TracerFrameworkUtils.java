package cn.sailstack.framework.tracer.core.util;

import io.micrometer.tracing.Span;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 链路追踪 Util
 *
 * @author 农E链源码
 */
public class TracerFrameworkUtils {

    /**
     * 将异常记录到 Span 中
     *
     * @param throwable 异常
     * @param span Span
     */
    public static void onError(Throwable throwable, Span span) {
        // 忽略无效 Span
        if (span == null || span.isNoop()) {
            return;
        }
        // 标记异常状态 + 记录异常事件（Micrometer Tracing 通过 error 统一处理）
        Throwable error = throwable != null ? throwable : new IllegalStateException("Biz span error");
        span.error(error);
        // 记录异常堆栈
        StringWriter sw = new StringWriter();
        error.printStackTrace(new PrintWriter(sw));
        span.tag("error.stack", sw.toString());
    }

}
