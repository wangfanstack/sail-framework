package cn.sailstack.module.infra.service.codegen.inner;

import cn.sailstack.framework.mybatis.core.dataobject.BaseDO;
import cn.sailstack.module.infra.convert.codegen.CodegenConvert;
import cn.sailstack.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.sailstack.module.infra.dal.dataobject.codegen.CodegenTableDO;
import cn.sailstack.module.infra.enums.codegen.CodegenColumnHtmlTypeEnum;
import cn.sailstack.module.infra.enums.codegen.CodegenColumnListConditionEnum;
import cn.sailstack.module.infra.enums.codegen.CodegenTemplateTypeEnum;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.commons.lang3.StringUtils.capitalize;

/**
 * 代码生成器的 Builder，负责：
 * 1. 将数据库的表 {@link TableInfo} 定义，构建成 {@link CodegenTableDO}
 * 2. 将数据库的列 {@link TableField} 构定义，建成 {@link CodegenColumnDO}
 */
@Component
public class CodegenBuilder {

    /**
     * 字段名与 {@link CodegenColumnListConditionEnum} 的默认映射
     * 注意，字段的匹配以后缀的方式
     */
    private static final Map<String, CodegenColumnListConditionEnum> COLUMN_LIST_OPERATION_CONDITION_MAPPINGS =
            Map.of("name", CodegenColumnListConditionEnum.LIKE,
                    "time", CodegenColumnListConditionEnum.BETWEEN,
                    "date", CodegenColumnListConditionEnum.BETWEEN);

    /**
     * 字段名与 {@link CodegenColumnHtmlTypeEnum} 的默认映射
     * 注意，字段的匹配以后缀的方式
     */
    private static final Map<String, CodegenColumnHtmlTypeEnum> COLUMN_HTML_TYPE_MAPPINGS =
            Map.of("status", CodegenColumnHtmlTypeEnum.RADIO,
                    "sex", CodegenColumnHtmlTypeEnum.RADIO,
                    "type", CodegenColumnHtmlTypeEnum.SELECT,
                    "image", CodegenColumnHtmlTypeEnum.IMAGE_UPLOAD,
                    "file", CodegenColumnHtmlTypeEnum.FILE_UPLOAD,
                    "content", CodegenColumnHtmlTypeEnum.EDITOR,
                    "description", CodegenColumnHtmlTypeEnum.EDITOR,
                    "demo", CodegenColumnHtmlTypeEnum.EDITOR,
                    "time", CodegenColumnHtmlTypeEnum.DATETIME,
                    "date", CodegenColumnHtmlTypeEnum.DATETIME);

    /**
     * 多租户编号的字段名
     */
    public static final String TENANT_ID_FIELD = "tenantId";
    /**
     * {@link cn.sailstack.framework.mybatis.core.dataobject.BaseDO} 的字段
     */
    public static final Set<String> BASE_DO_FIELDS = new HashSet<>();
    /**
     * 新增操作，不需要传递的字段
     */
    private static final Set<String> CREATE_OPERATION_EXCLUDE_COLUMN = Sets.newHashSet("id");
    /**
     * 修改操作，不需要传递的字段
     */
    private static final Set<String> UPDATE_OPERATION_EXCLUDE_COLUMN = Sets.newHashSet();
    /**
     * 列表操作的条件，不需要传递的字段
     */
    private static final Set<String> LIST_OPERATION_EXCLUDE_COLUMN = Sets.newHashSet("id");
    /**
     * 列表操作的结果，不需要返回的字段
     */
    private static final Set<String> LIST_OPERATION_RESULT_EXCLUDE_COLUMN = Sets.newHashSet();

    static {
        Arrays.stream(BaseDO.class.getDeclaredFields()).map(Field::getName).forEach(BASE_DO_FIELDS::add);
        BASE_DO_FIELDS.add(TENANT_ID_FIELD);
        // 处理 OPERATION 相关的字段
        CREATE_OPERATION_EXCLUDE_COLUMN.addAll(BASE_DO_FIELDS);
        UPDATE_OPERATION_EXCLUDE_COLUMN.addAll(BASE_DO_FIELDS);
        LIST_OPERATION_EXCLUDE_COLUMN.addAll(BASE_DO_FIELDS);
        LIST_OPERATION_EXCLUDE_COLUMN.remove("createTime"); // 创建时间，还是可能需要传递的
        LIST_OPERATION_RESULT_EXCLUDE_COLUMN.addAll(BASE_DO_FIELDS);
        LIST_OPERATION_RESULT_EXCLUDE_COLUMN.remove("createTime"); // 创建时间，还是需要返回的
    }

    public CodegenTableDO buildTable(TableInfo tableInfo) {
        CodegenTableDO table = CodegenConvert.INSTANCE.convert(tableInfo);
        initTableDefault(table);
        return table;
    }

    /**
     * 初始化 Table 表的默认字段
     *
     * @param table 表定义
     */
    private void initTableDefault(CodegenTableDO table) {
        // 以 system_dept 举例子。moduleName 为 system、businessName 为 dept、className 为 Dept
        // 如果希望以 System 前缀，则可以手动在【代码生成 - 修改生成配置 - 基本信息】，将实体类名称改为 SystemDept 即可
        String tableName = table.getTableName().toLowerCase();
        // 第一步，_ 前缀的前面，作为 module 名字；第二步，moduleName 必须小写；
        table.setModuleName(StringUtils.substringBefore(tableName, "_").toLowerCase());
        // 第一步，第一个 _ 前缀的后面，作为 module 名字; 第二步，可能存在多个 _ 的情况，转换成驼峰; 第三步，businessName 必须小写；
        table.setBusinessName(toCamelCase(StringUtils.substringAfter(tableName, "_")).toLowerCase());
        // 驼峰 + 首字母大写；第一步，第一个 _ 前缀的后面，作为 class 名字；第二步，驼峰命名
        table.setClassName(capitalize(toCamelCase(StringUtils.substringAfter(tableName, "_"))));
        // 去除结尾的表，作为类描述；注释中的英文引号替换为中文引号，避免破坏生成代码中的字符串字面量
        table.setClassComment(StringUtils.removeEndIgnoreCase(sanitizeComment(table.getTableComment()), "表"));
        table.setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
    }

    /**
     * 下划线转小驼峰，例如 sys_user_role 转 sysUserRole
     *
     * @param name 名称
     * @return 驼峰字符串
     */
    private static String toCamelCase(String name) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
    }

    public List<CodegenColumnDO> buildColumns(Long tableId, List<TableField> tableFields) {
        List<CodegenColumnDO> columns = CodegenConvert.INSTANCE.convertList(tableFields);
        int index = 1;
        for (CodegenColumnDO column : columns) {
            column.setTableId(tableId);
            column.setOrdinalPosition(index++);
            column.setColumnComment(sanitizeComment(column.getColumnComment()));
            // 特殊处理：Byte => Integer
            if (Byte.class.getSimpleName().equals(column.getJavaType())) {
                column.setJavaType(Integer.class.getSimpleName());
            }
            // 初始化 Column 列的默认字段
            processColumnOperation(column); // 处理 CRUD 相关的字段的默认值
            processColumnUI(column); // 处理 UI 相关的字段的默认值
            processColumnExample(column); // 处理字段的 swagger example 示例
        }
        return columns;
    }

    private void processColumnOperation(CodegenColumnDO column) {
        // 处理 createOperation 字段
        column.setCreateOperation(!CREATE_OPERATION_EXCLUDE_COLUMN.contains(column.getJavaField())
                && !column.getPrimaryKey()); // 对于主键，创建时无需传递
        // 处理 updateOperation 字段
        column.setUpdateOperation(!UPDATE_OPERATION_EXCLUDE_COLUMN.contains(column.getJavaField())
                || column.getPrimaryKey()); // 对于主键，更新时需要传递
        // 处理 listOperation 字段
        column.setListOperation(!LIST_OPERATION_EXCLUDE_COLUMN.contains(column.getJavaField())
                && !column.getPrimaryKey()); // 对于主键，列表过滤不需要传递
        // 处理 listOperationCondition 字段
        COLUMN_LIST_OPERATION_CONDITION_MAPPINGS.entrySet().stream()
                .filter(entry -> StringUtils.endsWithIgnoreCase(column.getJavaField(), entry.getKey()))
                .findFirst().ifPresent(entry -> column.setListOperationCondition(entry.getValue().getCondition()));
        if (column.getListOperationCondition() == null) {
            column.setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition());
        }
        // 处理 listOperationResult 字段
        column.setListOperationResult(!LIST_OPERATION_RESULT_EXCLUDE_COLUMN.contains(column.getJavaField()));
    }

    private void processColumnUI(CodegenColumnDO column) {
        // 基于后缀进行匹配
        COLUMN_HTML_TYPE_MAPPINGS.entrySet().stream()
                .filter(entry -> StringUtils.endsWithIgnoreCase(column.getJavaField(), entry.getKey()))
                .findFirst().ifPresent(entry -> column.setHtmlType(entry.getValue().getType()));
        // 如果是 Boolean 类型时，设置为 radio 类型.
        if (Boolean.class.getSimpleName().equals(column.getJavaType())) {
            column.setHtmlType(CodegenColumnHtmlTypeEnum.RADIO.getType());
        }
        // 如果是 LocalDateTime 类型，则设置为 datetime 类型
        if (LocalDateTime.class.getSimpleName().equals(column.getJavaType())) {
            column.setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        }
        // 兜底，设置默认为 input 类型
        if (column.getHtmlType() == null) {
            column.setHtmlType(CodegenColumnHtmlTypeEnum.INPUT.getType());
        }
    }

    /**
     * 处理字段的 swagger example 示例
     *
     * @param column 字段
     */
    private void processColumnExample(CodegenColumnDO column) {
        // id、price、count 等可能是整数的后缀
        if (StringUtils.endsWithAny(column.getJavaField(), "id", "price", "count")) {
            column.setExample(String.valueOf(ThreadLocalRandom.current().nextInt(1, Short.MAX_VALUE)));
            return;
        }
        // name
        if (StringUtils.endsWithIgnoreCase(column.getJavaField(), "name")) {
            column.setExample(randomEle(new String[]{"张三", "李四", "王五", "赵六", "芋艿"}));
            return;
        }
        // status
        if (StringUtils.endsWithAny(column.getJavaField(), "status", "type")) {
            column.setExample(randomEle(new String[]{"1", "2"}));
            return;
        }
        // url
        if (StringUtils.endsWithIgnoreCase(column.getColumnName(), "url")) {
            column.setExample("https://www.iocoder.cn");
            return;
        }
        // reason
        if (StringUtils.endsWithIgnoreCase(column.getColumnName(), "reason")) {
            column.setExample(randomEle(new String[]{"不喜欢", "不对", "不好", "不香"}));
            return;
        }
        // description、memo、remark
        if (StringUtils.endsWithAny(column.getColumnName(), "description", "memo", "remark")) {
            column.setExample(randomEle(new String[]{"你猜", "随便", "你说的对"}));
        }
    }

    /**
     * 从数组中随机取一个元素
     *
     * @param array 数组
     * @return 随机元素
     */
    private static String randomEle(String[] array) {
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }

    /**
     * 将注释中的英文引号替换为中文引号，避免破坏生成代码中的字符串字面量
     *
     * @param comment 原始注释
     * @return 清理后的注释
     */
    @VisibleForTesting
    String sanitizeComment(String comment) {
        if (StringUtils.isEmpty(comment)) {
            return comment;
        }
        return comment.replace('"', '“').replace('\'', '‘');
    }

}
