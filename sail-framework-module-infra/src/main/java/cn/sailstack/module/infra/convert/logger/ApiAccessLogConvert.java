package cn.sailstack.module.infra.convert.logger;

import cn.sailstack.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogRespVO;
import cn.sailstack.module.infra.dal.dataobject.logger.ApiAccessLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * API 访问日志 Convert
 *
 * @author 农E链源码
 */
@Mapper
public interface ApiAccessLogConvert {

    ApiAccessLogConvert INSTANCE = Mappers.getMapper(ApiAccessLogConvert.class);

    ApiAccessLogDO convert(ApiAccessLogCreateReqDTO bean);

    ApiAccessLogRespVO convert(ApiAccessLogDO bean);

    List<ApiAccessLogRespVO> convertList(List<ApiAccessLogDO> list);

    default PageResult<ApiAccessLogRespVO> convertPage(PageResult<ApiAccessLogDO> page) {
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

}
