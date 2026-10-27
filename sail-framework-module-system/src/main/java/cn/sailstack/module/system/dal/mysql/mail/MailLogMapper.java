package cn.sailstack.module.system.dal.mysql.mail;

import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.framework.mybatis.core.mapper.BaseMapperX;
import cn.sailstack.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.sailstack.framework.mybatis.core.util.MyBatisUtils;
import cn.sailstack.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import cn.sailstack.module.system.dal.dataobject.mail.MailLogDO;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MailLogMapper extends BaseMapperX<MailLogDO> {

    default PageResult<MailLogDO> selectPage(MailLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MailLogDO>()
                .eqIfPresent(MailLogDO::getUserId, reqVO.getUserId())
                .eqIfPresent(MailLogDO::getUserType, reqVO.getUserType())
                .eqIfPresent(MailLogDO::getAccountId, reqVO.getAccountId())
                .eqIfPresent(MailLogDO::getTemplateId, reqVO.getTemplateId())
                .eqIfPresent(MailLogDO::getSendStatus, reqVO.getSendStatus())
                .betweenIfPresent(MailLogDO::getSendTime, reqVO.getSendTime())
                .apply(StringUtils.isNotBlank(reqVO.getToMail()),
                        MyBatisUtils.findInSet("to_mails"), reqVO.getToMail())
                .orderByDesc(MailLogDO::getId));
    }

}
