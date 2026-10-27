package cn.sailstack.module.system.convert.logger;

import cn.sailstack.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.system.api.logger.dto.OperateLogRespDTO;
import cn.sailstack.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.sailstack.module.system.controller.admin.logger.vo.loginlog.LoginLogRespVO;
import cn.sailstack.module.system.controller.admin.logger.vo.operatelog.OperateLogRespVO;
import cn.sailstack.module.system.dal.dataobject.logger.LoginLogDO;
import cn.sailstack.module.system.dal.dataobject.logger.OperateLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface OperateLogConvert {

    OperateLogConvert INSTANCE = Mappers.getMapper(OperateLogConvert.class);

    OperateLogRespDTO convertToRespDTO(OperateLogDO bean);

    List<OperateLogRespDTO> convertRespDTOList(List<OperateLogDO> list);

    default PageResult<OperateLogRespDTO> convertRespDTOPage(PageResult<OperateLogDO> page) {
        return new PageResult<>(convertRespDTOList(page.getList()), page.getTotal());
    }

    OperateLogDO convertToDO(OperateLogCreateReqDTO bean);

    OperateLogRespVO convertToRespVO(OperateLogDO bean);

    List<OperateLogRespVO> convertRespVOList(List<OperateLogDO> list);

    default PageResult<OperateLogRespVO> convertRespVOPage(PageResult<OperateLogDO> page) {
        return new PageResult<>(convertRespVOList(page.getList()), page.getTotal());
    }

    LoginLogDO convertToLoginLogDO(LoginLogCreateReqDTO bean);

    LoginLogRespVO convertToLoginLogRespVO(LoginLogDO bean);

    List<LoginLogRespVO> convertLoginLogRespVOList(List<LoginLogDO> list);

    default PageResult<LoginLogRespVO> convertLoginLogRespVOPage(PageResult<LoginLogDO> page) {
        return new PageResult<>(convertLoginLogRespVOList(page.getList()), page.getTotal());
    }

}
