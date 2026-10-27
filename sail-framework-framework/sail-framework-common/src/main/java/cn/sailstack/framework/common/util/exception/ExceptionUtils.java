package cn.sailstack.framework.common.util.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常工具类
 *
 * @author 农E链源码
 */
public class ExceptionUtils {

    /**
     * 获得异常的消息
     *
     * @param e 异常
     * @return 消息
     */
    public static String getMessage(Throwable e) {
        if (e == null) {
            return "";
        }
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }

    /**
     * 获得根因异常的消息
     *
     * @param e 异常
     * @return 根因消息
     */
    public static String getRootCauseMessage(Throwable e) {
        return getMessage(getRootCause(e));
    }

    /**
     * 获得根因异常
     *
     * @param e 异常
     * @return 根因异常；无 cause 时返回自身
     */
    public static Throwable getRootCause(Throwable e) {
        if (e == null) {
            return null;
        }
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root;
    }

    /**
     * 堆栈转为字符串
     *
     * @param e 异常
     * @return 堆栈字符串
     */
    public static String stacktraceToString(Throwable e) {
        if (e == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}
