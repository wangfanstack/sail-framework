package cn.sailstack.framework.ratelimiter.core.keyresolver.impl;

import cn.sailstack.framework.common.util.string.DigestUtils;
import cn.sailstack.framework.common.util.string.StrUtils;
import cn.sailstack.framework.ratelimiter.core.annotation.RateLimiter;
import cn.sailstack.framework.ratelimiter.core.keyresolver.RateLimiterKeyResolver;
import org.aspectj.lang.JoinPoint;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Server 节点级别的限流 Key 解析器，使用方法名 + 方法参数 + IP，组装成一个 Key
 *
 * 为了避免 Key 过长，使用 MD5 进行“压缩”
 *
 * @author 农E链源码
 */
public class ServerNodeRateLimiterKeyResolver implements RateLimiterKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, RateLimiter rateLimiter) {
        String methodName = joinPoint.getSignature().toString();
        String argsStr = StrUtils.joinMethodArgs(joinPoint);
        String serverNode = String.format("%s@%d", getHostAddress(), ProcessHandle.current().pid());
        return DigestUtils.md5Hex(methodName + argsStr + serverNode);
    }

    private static String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

}
