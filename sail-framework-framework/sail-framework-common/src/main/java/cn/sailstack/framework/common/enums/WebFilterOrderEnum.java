package cn.sailstack.framework.common.enums;

/**
 * Web 过滤器顺序的枚举类，保证过滤器按照符合我们的预期
 *
 *  考虑到每个 starter 都需要用到该工具类，所以放到 common 模块下的 enums 包下
 *
 * @author 农E链源码
 */
public interface WebFilterOrderEnum {

    int CORS_FILTER = Integer.MIN_VALUE;

    // 注意：必须是 CORS_FILTER + 2。Spring Boot 的 ServerHttpObservationFilter（链路追踪的过滤器）
    // 固定使用 HIGHEST_PRECEDENCE + 1，TRACE_FILTER 必须排在它之后，才能从 MDC 中读取到 traceId
    int TRACE_FILTER = CORS_FILTER + 2;

    int REQUEST_BODY_CACHE_FILTER = Integer.MIN_VALUE + 500;

    int API_ENCRYPT_FILTER = REQUEST_BODY_CACHE_FILTER + 1;

    // OrderedRequestContextFilter 默认为 -105，用于国际化上下文等等

    int TENANT_CONTEXT_FILTER = - 104; // 需要保证在 ApiAccessLogFilter 前面

    int API_ACCESS_LOG_FILTER = -103; // 需要保证在 RequestBodyCacheFilter 后面

    int XSS_FILTER = -102;  // 需要保证在 RequestBodyCacheFilter 后面

    // Spring Security Filter 默认为 -100，可见 org.springframework.boot.autoconfigure.security.SecurityProperties 配置属性类

    int TENANT_SECURITY_FILTER = -99; // 需要保证在 Spring Security 过滤器后面

    int FLOWABLE_FILTER = -98; // 需要保证在 Spring Security 过滤后面

    int DEMO_FILTER = Integer.MAX_VALUE;

}
