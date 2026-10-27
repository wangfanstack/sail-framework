package cn.sailstack.module.infra.convert.demo;

import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.infra.controller.admin.demo.demo01.vo.Demo01ContactRespVO;
import cn.sailstack.module.infra.controller.admin.demo.demo01.vo.Demo01ContactSaveReqVO;
import cn.sailstack.module.infra.dal.dataobject.demo.demo01.Demo01ContactDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 示例联系人 Convert
 *
 * @author 农E链源码
 */
@Mapper
public interface Demo01ContactConvert {

    Demo01ContactConvert INSTANCE = Mappers.getMapper(Demo01ContactConvert.class);

    Demo01ContactDO convert(Demo01ContactSaveReqVO bean);

    Demo01ContactRespVO convert(Demo01ContactDO bean);

    List<Demo01ContactRespVO> convertList(List<Demo01ContactDO> list);

    default PageResult<Demo01ContactRespVO> convertPage(PageResult<Demo01ContactDO> page) {
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

}
