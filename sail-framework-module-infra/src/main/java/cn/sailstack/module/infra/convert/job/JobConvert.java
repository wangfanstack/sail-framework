package cn.sailstack.module.infra.convert.job;

import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.infra.controller.admin.job.vo.job.JobRespVO;
import cn.sailstack.module.infra.controller.admin.job.vo.job.JobSaveReqVO;
import cn.sailstack.module.infra.controller.admin.job.vo.log.JobLogRespVO;
import cn.sailstack.module.infra.dal.dataobject.job.JobDO;
import cn.sailstack.module.infra.dal.dataobject.job.JobLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 定时任务 Convert
 *
 * @author 农E链源码
 */
@Mapper
public interface JobConvert {

    JobConvert INSTANCE = Mappers.getMapper(JobConvert.class);

    JobDO convert(JobSaveReqVO bean);

    JobRespVO convert(JobDO bean);

    List<JobRespVO> convertList(List<JobDO> list);

    default PageResult<JobRespVO> convertPage(PageResult<JobDO> page) {
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

    JobLogRespVO convert(JobLogDO bean);

    List<JobLogRespVO> convertLogList(List<JobLogDO> list);

    default PageResult<JobLogRespVO> convertLogPage(PageResult<JobLogDO> page) {
        return new PageResult<>(convertLogList(page.getList()), page.getTotal());
    }

}
