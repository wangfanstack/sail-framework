package cn.sailstack.framework.datasource.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 数据库配置类
 *
 * 连接池使用 Spring Boot 默认的 HikariCP，数据源由 spring.datasource 标准配置项提供
 *
 * @author 农E链源码
 */
@AutoConfiguration
@EnableTransactionManagement(proxyTargetClass = true) // 启动事务管理
public class Sail-frameworkDataSourceAutoConfiguration {
}
