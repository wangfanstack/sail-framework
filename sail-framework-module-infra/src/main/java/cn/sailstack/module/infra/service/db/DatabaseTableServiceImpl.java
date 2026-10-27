package cn.sailstack.module.infra.service.db;

import cn.sailstack.framework.mybatis.core.util.JdbcUtils;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.query.SQLQuery;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库表 Service 实现类
 *
 * @author 农E链源码
 */
@Service
public class DatabaseTableServiceImpl implements DatabaseTableService {

    /**
     * Spring 主数据源（HikariCP）
     */
    @Resource
    private DataSource dataSource;

    @Override
    public List<TableInfo> getTableList(String nameLike, String commentLike) {
        List<TableInfo> tables = getTableList0(null);
        return tables.stream().filter(tableInfo -> (StringUtils.isEmpty(nameLike) || tableInfo.getName().contains(nameLike))
                        && (StringUtils.isEmpty(commentLike) || tableInfo.getComment().contains(commentLike)))
                .collect(Collectors.toList());
    }

    @Override
    public TableInfo getTable(String name) {
        List<TableInfo> tables = getTableList0(name);
        return tables.isEmpty() ? null : tables.get(0);
    }

    private List<TableInfo> getTableList0(String name) {
        // 使用 MyBatis Plus Generator 解析表结构，直接基于 Spring 主数据源获取 Connection
        DbType dbType = JdbcUtils.getDbType();
        DataSourceConfig.Builder dataSourceConfigBuilder = new DataSourceConfig.Builder(dataSource);
        if (JdbcUtils.isSQLServer(dbType)) { // 特殊：SQLServer jdbc 非标准，参见 https://github.com/baomidou/mybatis-plus/issues/5419
            dataSourceConfigBuilder.databaseQueryClass(SQLQuery.class);
        }
        StrategyConfig.Builder strategyConfig = new StrategyConfig.Builder().enableSkipView(); // 忽略视图，业务上一般用不到
        if (StringUtils.isNotEmpty(name)) {
            strategyConfig.addInclude(name);
        } else {
            // 移除工作流和定时任务前缀的表名
            strategyConfig.addExclude("ACT_[\\S\\s]+|QRTZ_[\\S\\s]+|FLW_[\\S\\s]+|act_[\\S\\s]+|qrtz_[\\S\\s]+|flw_[\\S\\s]+");
            // 移除 ORACLE 相关的系统表
            strategyConfig.addExclude("IMPDP_[\\S\\s]+|ALL_[\\S\\s]+|HS_[\\S\\s]+|impdp_[\\S\\s]+|all_[\\S\\s]+|hs_[\\S\\s]+");
            strategyConfig.addExclude("[\\S\\s]+\\$[\\S\\s]+|[\\S\\s]+\\$"); // 表里不能有 $，一般有都是系统的表
        }

        GlobalConfig globalConfig = new GlobalConfig.Builder().dateType(DateType.TIME_PACK).build(); // 只使用 LocalDateTime 类型，不使用 LocalDate
        ConfigBuilder builder = new ConfigBuilder(null, dataSourceConfigBuilder.build(), strategyConfig.build(),
                null, globalConfig, null);
        // 按照名字排序
        List<TableInfo> tables = builder.getTableInfoList();
        tables.sort(Comparator.comparing(TableInfo::getName));
        return tables;
    }

}
