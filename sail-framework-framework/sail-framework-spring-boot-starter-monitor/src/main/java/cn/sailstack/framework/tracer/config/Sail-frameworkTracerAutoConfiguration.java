package cn.sailstack.framework.tracer.config;

import cn.sailstack.framework.common.enums.WebFilterOrderEnum;
import cn.sailstack.framework.tracer.core.aop.BizTraceAspect;
import cn.sailstack.framework.tracer.core.filter.TraceFilter;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Tracer 配置类
 *
 * io.micrometer.tracing.Tracer Bean 由 Spring Boot 的
 * org.springframework.boot.actuate.autoconfigure.tracing.BraveAutoConfiguration 提供
 * （依赖 micrometer-tracing-bridge-brave），此处只负责项目自己的增强组件
 *
 * @author mashu
 */
@AutoConfiguration
@ConditionalOnClass(name = {
        "io.micrometer.tracing.Tracer", // 来自 micrometer-tracing.jar（由 micrometer-tracing-bridge-brave 引入）
        "jakarta.servlet.Filter"
})
@EnableConfigurationProperties(TracerProperties.class)
@ConditionalOnProperty(prefix = "sail-framework.tracer", value = "enable", matchIfMissing = true)
public class Sail-frameworkTracerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BizTraceAspect bizTracingAop(Tracer tracer) {
        return new BizTraceAspect(tracer);
    }

    /**
     * 创建 TraceFilter 过滤器，响应 header 设置 traceId
     */
    @Bean
    public FilterRegistrationBean<TraceFilter> traceFilter() {
        FilterRegistrationBean<TraceFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TraceFilter());
        registrationBean.setOrder(WebFilterOrderEnum.TRACE_FILTER);
        return registrationBean;
    }

}
