package cn.sailstack.module.infra.convert.file;

import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.infra.controller.admin.file.vo.config.FileConfigRespVO;
import cn.sailstack.module.infra.controller.admin.file.vo.config.FileConfigSaveReqVO;
import cn.sailstack.module.infra.dal.dataobject.file.FileConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 文件配置 Convert
 *
 * @author 农E链源码
 */
@Mapper
public interface FileConfigConvert {

    FileConfigConvert INSTANCE = Mappers.getMapper(FileConfigConvert.class);

    @Mapping(target = "config", ignore = true)
    FileConfigDO convert(FileConfigSaveReqVO bean);

    FileConfigRespVO convert(FileConfigDO bean);

    List<FileConfigRespVO> convertList(List<FileConfigDO> list);

    default PageResult<FileConfigRespVO> convertPage(PageResult<FileConfigDO> page) {
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

}
