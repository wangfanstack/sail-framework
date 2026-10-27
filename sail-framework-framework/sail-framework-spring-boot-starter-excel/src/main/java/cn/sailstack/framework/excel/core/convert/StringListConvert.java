package cn.sailstack.framework.excel.core.convert;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Excel 字符串列表转换器
 *
 * @author 农E链源码
 */
public class StringListConvert implements Converter<List<String>> {

    /**
     * 将 Excel 单元格中的分隔字符串解析为字符串列表。
     *
     * @param value               Excel 单元格数据
     * @param contentProperty     Excel 字段属性
     * @param globalConfiguration Excel 全局配置
     * @return 字符串列表
     */
    @Override
    public List<String> convertToJavaData(ReadCellData<?> value, ExcelContentProperty contentProperty,
                                          GlobalConfiguration globalConfiguration) {
        String text = value == null ? null : value.getStringValue();
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        // 兼容导入模板常用的英文逗号、中文逗号、顿号，以及导出时使用的斜杠。
        return Arrays.stream(text.replace('，', ',').replace('、', ',').replace('/', ',').split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    @Override
    public WriteCellData<String> convertToExcelData(List<String> value,
            ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(value == null || value.isEmpty() ? "" : String.join("/", value));
    }

}
