package cn.sailstack.module.system.framework.web.config;

import cn.sailstack.framework.swagger.config.Sail-frameworkSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system 模块的 web 组件的 Configuration
 *
 * @author 农E链源码
 */
@Configuration(proxyBeanMethods = false)
public class SystemWebConfiguration {

    /**
     * system 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi systemGroupedOpenApi() {
        return Sail-frameworkSwaggerAutoConfiguration.buildGroupedOpenApi("system");
    }

}
