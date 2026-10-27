package cn.sailstack.module.infra.convert.codegen;

import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.infra.controller.admin.codegen.vo.CodegenDetailRespVO;
import cn.sailstack.module.infra.controller.admin.codegen.vo.CodegenPreviewRespVO;
import cn.sailstack.module.infra.controller.admin.codegen.vo.column.CodegenColumnRespVO;
import cn.sailstack.module.infra.controller.admin.codegen.vo.column.CodegenColumnSaveReqVO;
import cn.sailstack.module.infra.controller.admin.codegen.vo.table.CodegenTableRespVO;
import cn.sailstack.module.infra.controller.admin.codegen.vo.table.CodegenTableSaveReqVO;
import cn.sailstack.module.infra.controller.admin.codegen.vo.table.DatabaseTableRespVO;
import cn.sailstack.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.sailstack.module.infra.dal.dataobject.codegen.CodegenTableDO;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import org.apache.ibatis.type.JdbcType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

@Mapper
public interface CodegenConvert {

    CodegenConvert INSTANCE = Mappers.getMapper(CodegenConvert.class);

    // ========== TableInfo 相关 ==========

    @Mappings({
            @Mapping(source = "name", target = "tableName"),
            @Mapping(source = "comment", target = "tableComment"),
    })
    CodegenTableDO convert(TableInfo bean);

    List<CodegenColumnDO> convertList(List<TableField> list);

    @Mappings({
            @Mapping(source = "name", target = "columnName"),
            @Mapping(source = "metaInfo.jdbcType", target = "dataType", qualifiedByName = "getDataType"),
            @Mapping(source = "comment", target = "columnComment"),
            @Mapping(source = "metaInfo.nullable", target = "nullable"),
            @Mapping(source = "keyFlag", target = "primaryKey"),
            @Mapping(source = "columnType.type", target = "javaType"),
            @Mapping(source = "propertyName", target = "javaField"),
    })
    CodegenColumnDO convert(TableField bean);

    @Named("getDataType")
    default String getDataType(JdbcType jdbcType) {
        return jdbcType.name();
    }

    DatabaseTableRespVO convertDatabaseTable(TableInfo bean);

    List<DatabaseTableRespVO> convertDatabaseTableList(List<TableInfo> list);

    // ========== SaveReqVO -> DO ==========

    CodegenTableDO convert(CodegenTableSaveReqVO bean);

    CodegenColumnDO convert(CodegenColumnSaveReqVO bean);

    List<CodegenColumnDO> convertColumnList(List<CodegenColumnSaveReqVO> list);

    // ========== DO -> RespVO ==========

    CodegenTableRespVO convertTable(CodegenTableDO bean);

    List<CodegenTableRespVO> convertTableList(List<CodegenTableDO> list);

    default PageResult<CodegenTableRespVO> convertTablePage(PageResult<CodegenTableDO> page) {
        return new PageResult<>(convertTableList(page.getList()), page.getTotal());
    }

    CodegenColumnRespVO convertColumn(CodegenColumnDO bean);

    List<CodegenColumnRespVO> convertColumnRespList(List<CodegenColumnDO> list);

    // ========== 其它 ==========

    default CodegenDetailRespVO convert(CodegenTableDO table, List<CodegenColumnDO> columns) {
        CodegenDetailRespVO respVO = new CodegenDetailRespVO();
        respVO.setTable(convertTable(table));
        respVO.setColumns(convertColumnRespList(columns));
        return respVO;
    }

    default List<CodegenPreviewRespVO> convert(Map<String, String> codes) {
        return codes.entrySet().stream()
                .map(entry -> new CodegenPreviewRespVO().setFilePath(entry.getKey()).setCode(entry.getValue()))
                .collect(java.util.stream.Collectors.toList());
    }

}
