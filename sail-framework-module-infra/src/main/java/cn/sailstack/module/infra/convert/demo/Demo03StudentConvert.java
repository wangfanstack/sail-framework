package cn.sailstack.module.infra.convert.demo;

import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.infra.controller.admin.demo.demo03.erp.vo.Demo03StudentErpRespVO;
import cn.sailstack.module.infra.controller.admin.demo.demo03.erp.vo.Demo03StudentErpSaveReqVO;
import cn.sailstack.module.infra.controller.admin.demo.demo03.inner.vo.Demo03StudentInnerRespVO;
import cn.sailstack.module.infra.controller.admin.demo.demo03.inner.vo.Demo03StudentInnerSaveReqVO;
import cn.sailstack.module.infra.controller.admin.demo.demo03.normal.vo.Demo03StudentNormalRespVO;
import cn.sailstack.module.infra.controller.admin.demo.demo03.normal.vo.Demo03StudentNormalSaveReqVO;
import cn.sailstack.module.infra.dal.dataobject.demo.demo03.Demo03StudentDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 示例学生 Convert
 *
 * @author 农E链源码
 */
@Mapper
public interface Demo03StudentConvert {

    Demo03StudentConvert INSTANCE = Mappers.getMapper(Demo03StudentConvert.class);

    Demo03StudentDO convert(Demo03StudentErpSaveReqVO bean);

    Demo03StudentDO convert(Demo03StudentInnerSaveReqVO bean);

    Demo03StudentDO convert(Demo03StudentNormalSaveReqVO bean);

    Demo03StudentErpRespVO convertErp(Demo03StudentDO bean);

    List<Demo03StudentErpRespVO> convertErpList(List<Demo03StudentDO> list);

    default PageResult<Demo03StudentErpRespVO> convertErpPage(PageResult<Demo03StudentDO> page) {
        return new PageResult<>(convertErpList(page.getList()), page.getTotal());
    }

    Demo03StudentInnerRespVO convertInner(Demo03StudentDO bean);

    List<Demo03StudentInnerRespVO> convertInnerList(List<Demo03StudentDO> list);

    default PageResult<Demo03StudentInnerRespVO> convertInnerPage(PageResult<Demo03StudentDO> page) {
        return new PageResult<>(convertInnerList(page.getList()), page.getTotal());
    }

    Demo03StudentNormalRespVO convertNormal(Demo03StudentDO bean);

    List<Demo03StudentNormalRespVO> convertNormalList(List<Demo03StudentDO> list);

    default PageResult<Demo03StudentNormalRespVO> convertNormalPage(PageResult<Demo03StudentDO> page) {
        return new PageResult<>(convertNormalList(page.getList()), page.getTotal());
    }

}
