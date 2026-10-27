package cn.sailstack.module.infra.framework.file.config;

import cn.sailstack.module.infra.framework.file.core.client.FileClientFactory;
import cn.sailstack.module.infra.framework.file.core.client.FileClientFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件配置类
 *
 * @author 农E链源码
 */
@Configuration(proxyBeanMethods = false)
public class Sail-frameworkFileAutoConfiguration {

    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }

}
