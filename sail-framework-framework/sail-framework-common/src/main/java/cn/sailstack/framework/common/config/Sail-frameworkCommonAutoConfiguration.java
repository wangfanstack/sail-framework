package cn.sailstack.framework.common.config;

import cn.sailstack.framework.common.util.spring.SpringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * common 模块的自动配置类
 *
 * @author 农E链源码
 */
@AutoConfiguration
public class Sail-frameworkCommonAutoConfiguration {

    /**
     * 注册 SpringUtils，使其以静态方式持有容器上下文
     */
    @Bean
    public SpringUtils springUtils() {
        return new SpringUtils();
    }

}
