package cn.sailstack.framework.common.util.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Objects;

/**
 * Spring 工具类
 *
 * 以静态方式持有 Spring 容器上下文，提供随处可用的 Bean / 环境配置访问能力。
 * 由 Sail-frameworkCommonAutoConfiguration 注册为 Bean，容器启动时完成注入。
 *
 * @author 农E链源码
 */
public class SpringUtils implements ApplicationContextAware, EnvironmentAware {

    private static ApplicationContext applicationContext;
    private static Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        SpringUtils.environment = environment;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static String getProperty(String key) {
        return environment != null ? environment.getProperty(key) : null;
    }

    public static String getActiveProfile() {
        return environment != null && environment.getActiveProfiles().length > 0
                ? environment.getActiveProfiles()[0] : null;
    }

    /**
     * 是否为生产环境
     *
     * @return 是否生产环境
     */
    public static boolean isProd() {
        return Objects.equals("prod", getActiveProfile());
    }

}
