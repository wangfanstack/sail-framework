package cn.sailstack.framework.common.util.object;

import java.util.Arrays;

/**
 * Object 工具类
 *
 * @author 农E链源码
 */
public class ObjectUtils {

    public static <T extends Comparable<T>> T max(T obj1, T obj2) {
        if (obj1 == null) {
            return obj2;
        }
        if (obj2 == null) {
            return obj1;
        }
        return obj1.compareTo(obj2) > 0 ? obj1 : obj2;
    }

    @SafeVarargs
    public static <T> T defaultIfNull(T... array) {
        for (T item : array) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    @SafeVarargs
    public static <T> boolean equalsAny(T obj, T... array) {
        return Arrays.asList(array).contains(obj);
    }

    @SafeVarargs
    public static <T> boolean notEqualsAny(T obj, T... array) {
        return !Arrays.asList(array).contains(obj);
    }

    public static boolean isNotAllEmpty(Object... objs) {
        for (Object obj : objs) {
            if (obj != null && !obj.toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }

}
