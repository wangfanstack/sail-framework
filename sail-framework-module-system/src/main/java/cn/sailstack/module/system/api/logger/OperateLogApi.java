package cn.sailstack.module.system.api.logger;

import cn.sailstack.framework.common.biz.system.logger.OperateLogCommonApi;
import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.system.api.logger.dto.OperateLogPageReqDTO;
import cn.sailstack.module.system.api.logger.dto.OperateLogRespDTO;

/**
 * 操作日志 API 接口
 *
 * @author 农E链源码
 */
public interface OperateLogApi extends OperateLogCommonApi {

    /**
     * 获取指定模块的指定数据的操作日志分页
     *
     * @param pageReqDTO 请求
     * @return 操作日志分页
     */
    PageResult<OperateLogRespDTO> getOperateLogPage(OperateLogPageReqDTO pageReqDTO);

}
