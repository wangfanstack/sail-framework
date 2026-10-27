package cn.sailstack.framework.ip.core.utils;

import cn.sailstack.framework.ip.core.Area;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.IOException;
import java.io.InputStream;

/**
 * IP 工具类
 *
 * IP 数据源来自 ip2region.xdb 精简版，基于 <a href="https://gitee.com/zhijiantianya/ip2region"/> 项目
 *
 * @author wanglhup
 */
@Slf4j
@UtilityClass
public class IPUtils {

    /**
     * IP 查询器，启动加载到内存中
     */
    private static Searcher SEARCHER;

    static {
        init();
    }

    /**
     * 初始化
     */
    private static void init() {
        try {
            long now = System.currentTimeMillis();
            byte[] bytes = readResourceBytes("ip2region.xdb");
            SEARCHER = Searcher.newWithBuffer(bytes);
            log.info("启动加载 IPUtils 成功，耗时 ({}) 毫秒", System.currentTimeMillis() - now);
        } catch (Exception e) {
            throw new RuntimeException("IPUtils 初始化失败", e);
        }
    }

    /**
     * 查询 IP 对应的地区编号
     *
     * @param ip IP 地址，格式为 127.0.0.1
     * @return 地区id
     */
    @SneakyThrows
    public static Integer getAreaId(String ip) {
        return Integer.parseInt(SEARCHER.search(ip.trim()));
    }

    /**
     * 查询 IP 对应的地区编号
     *
     * @param ip IP 地址的时间戳，格式参考{@link Searcher#checkIP(String)} 的返回
     * @return 地区编号
     */
    @SneakyThrows
    public static Integer getAreaId(long ip) {
        return Integer.parseInt(SEARCHER.search(ip));
    }

    /**
     * 查询 IP 对应的地区
     *
     * @param ip IP 地址，格式为 127.0.0.1
     * @return 地区
     */
    public static Area getArea(String ip) {
        return AreaUtils.getArea(getAreaId(ip));
    }

    /**
     * 查询 IP 对应的地区
     *
     * @param ip IP 地址的时间戳，格式参考{@link Searcher#checkIP(String)} 的返回
     * @return 地区
     */
    public static Area getArea(long ip) {
        return AreaUtils.getArea(getAreaId(ip));
    }

    /**
     * 读取 classpath 下的资源文件内容
     */
    private static byte[] readResourceBytes(String path) throws IOException {
        try (InputStream in = IPUtils.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("资源不存在：" + path);
            }
            return in.readAllBytes();
        }
    }
}
