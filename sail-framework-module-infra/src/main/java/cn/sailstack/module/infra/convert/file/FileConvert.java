package cn.sailstack.module.infra.convert.file;

import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import cn.sailstack.module.infra.controller.admin.file.vo.file.FileRespVO;
import cn.sailstack.module.infra.dal.dataobject.file.FileDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 文件 Convert
 *
 * @author 农E链源码
 */
@Mapper
public interface FileConvert {

    FileConvert INSTANCE = Mappers.getMapper(FileConvert.class);

    FileDO convert(FileCreateReqVO bean);

    FileRespVO convert(FileDO bean);

    List<FileRespVO> convertList(List<FileDO> list);

    default PageResult<FileRespVO> convertPage(PageResult<FileDO> page) {
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

}
