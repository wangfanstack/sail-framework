package cn.sailstack.framework.banner.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * 项目启动成功后，提供文档相关的地址
 *
 * @author 农E链源码
 */
@Slf4j
public class BannerApplicationRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000L); // 延迟 1 秒，保证输出到结尾
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
            log.info("\n----------------------------------------------------------\n\t" +
                            "项目启动成功！\n\t" +
                            "接口文档: \t{} \n\t" +
                            "开发文档: \t{} \n" +
                            "----------------------------------------------------------",
                    "https://doc.iocoder.cn/api-doc/",
                    "https://doc.iocoder.cn");
        }, "banner");
        thread.setDaemon(true);
        thread.start();
    }

}
