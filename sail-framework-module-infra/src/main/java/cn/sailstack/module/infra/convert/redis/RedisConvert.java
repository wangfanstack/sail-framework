package cn.sailstack.module.infra.convert.redis;

import cn.sailstack.module.infra.controller.admin.redis.vo.RedisMonitorRespVO;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Properties;

@Mapper
public interface RedisConvert {

    RedisConvert INSTANCE = Mappers.getMapper(RedisConvert.class);

    default RedisMonitorRespVO build(Properties info, Long dbSize, Properties commandStats) {
        RedisMonitorRespVO respVO = RedisMonitorRespVO.builder().info(info).dbSize(dbSize)
                .commandStats(new ArrayList<>(commandStats.size())).build();
        commandStats.forEach((key, value) -> {
            respVO.getCommandStats().add(RedisMonitorRespVO.CommandStat.builder()
                    .command(StringUtils.substringAfter((String) key, "cmdstat_"))
                    .calls(Long.valueOf(StringUtils.substringBetween((String) value, "calls=", ",")))
                    .usec(Long.valueOf(StringUtils.substringBetween((String) value, "usec=", ",")))
                    .build());
        });
        return respVO;
    }

}
