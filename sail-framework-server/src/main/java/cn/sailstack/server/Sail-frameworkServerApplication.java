package cn.sailstack.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 *
 * @author 农E链源码
 */
@SpringBootApplication(scanBasePackages = {"${sail-framework.info.base-package}.server", "${sail-framework.info.base-package}.module"})
public class Sail-frameworkServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(Sail-frameworkServerApplication.class, args);
    }

}
