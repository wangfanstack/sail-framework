package cn.sailstack.framework.ratelimiter.core.keyresolver.impl;

import cn.sailstack.framework.common.util.string.DigestUtils;
import cn.sailstack.framework.common.util.string.StrUtils;
import cn.sailstack.framework.ratelimiter.core.annotation.RateLimiter;
import cn.sailstack.framework.ratelimiter.core.keyresolver.RateLimiterKeyResolver;
import cn.sailstack.framework.web.core.util.WebFrameworkUtils;
import org.aspectj.lang.JoinPoint;

/**
 * 用户级别的限流 Key 解析器，使用方法名 + 方法参数 + userId + userType，组装成一个 Key
 *
 * 为了避免 Key 过长，使用 MD5 进行“压缩”
 *
 * @author 农E链源码
 */
public class UserRateLimiterKeyResolver implements RateLimiterKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, RateLimiter rateLimiter) {
        String methodName = joinPoint.getSignature().toString();
        String argsStr = StrUtils.joinMethodArgs(joinPoint);
        Long userId = WebFrameworkUtils.getLoginUserId();
        Integer userType = WebFrameworkUtils.getLoginUserType();
        return DigestUtils.md5Hex(methodName + argsStr + userId + userType);
    }

}
