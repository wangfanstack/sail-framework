package cn.sailstack.module.system.service.member;

import cn.sailstack.framework.common.util.spring.SpringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * Member Service 实现类
 *
 * @author 农E链源码
 */
@Service
public class MemberServiceImpl implements MemberService {

    @Value("${sail-framework.info.base-package}")
    private String basePackage;

    private volatile Object memberUserApi;

    @Override
    public String getMemberUserMobile(Long id) {
        Object user = getMemberUser(id);
        if (user == null) {
            return null;
        }
        return (String) invokeMethod(user, "getMobile");
    }

    @Override
    public String getMemberUserEmail(Long id) {
        Object user = getMemberUser(id);
        if (user == null) {
            return null;
        }
        return (String) invokeMethod(user, "getEmail");
    }

    private Object getMemberUser(Long id) {
        if (id == null) {
            return null;
        }
        return invokeMethod(getMemberUserApi(), "getUser", id);
    }

    private Object getMemberUserApi() {
        if (memberUserApi == null) {
            memberUserApi = SpringUtils.getBean(loadClass(String.format("%s.module.member.api.user.MemberUserApi", basePackage)));
        }
        return memberUserApi;
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.format("加载类(%s)失败", className), e);
        }
    }

    /**
     * 反射调用方法，语义等价于 hutool 的 ReflectUtil.invoke
     */
    private static Object invokeMethod(Object object, String methodName, Object... args) {
        try {
            Method method = null;
            for (Method candidate : object.getClass().getMethods()) {
                if (candidate.getName().equals(methodName) && candidate.getParameterCount() == args.length) {
                    method = candidate;
                    break;
                }
            }
            if (method == null) {
                throw new NoSuchMethodException(String.format("%s.%s", object.getClass().getName(), methodName));
            }
            method.setAccessible(true);
            return method.invoke(object, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(String.format("反射调用方法(%s)失败", methodName), e);
        }
    }

}
