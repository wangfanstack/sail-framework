package cn.sailstack.module.system.convert.sms;

import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.system.controller.admin.sms.vo.channel.SmsChannelRespVO;
import cn.sailstack.module.system.controller.admin.sms.vo.channel.SmsChannelSaveReqVO;
import cn.sailstack.module.system.controller.admin.sms.vo.channel.SmsChannelSimpleRespVO;
import cn.sailstack.module.system.controller.admin.sms.vo.log.SmsLogRespVO;
import cn.sailstack.module.system.controller.admin.sms.vo.template.SmsTemplateRespVO;
import cn.sailstack.module.system.controller.admin.sms.vo.template.SmsTemplateSaveReqVO;
import cn.sailstack.module.system.controller.admin.sms.vo.template.SmsTemplateSimpleRespVO;
import cn.sailstack.module.system.dal.dataobject.sms.SmsChannelDO;
import cn.sailstack.module.system.dal.dataobject.sms.SmsLogDO;
import cn.sailstack.module.system.dal.dataobject.sms.SmsTemplateDO;
import cn.sailstack.module.system.framework.sms.core.property.SmsChannelProperties;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SmsConvert {

    SmsConvert INSTANCE = Mappers.getMapper(SmsConvert.class);

    SmsTemplateDO convertToTemplateDO(SmsTemplateSaveReqVO bean);

    SmsTemplateRespVO convertToTemplateRespVO(SmsTemplateDO bean);

    List<SmsTemplateRespVO> convertTemplateRespVOList(List<SmsTemplateDO> list);

    List<SmsTemplateSimpleRespVO> convertTemplateSimpleRespVOList(List<SmsTemplateDO> list);

    default PageResult<SmsTemplateRespVO> convertTemplateRespVOPage(PageResult<SmsTemplateDO> page) {
        return new PageResult<>(convertTemplateRespVOList(page.getList()), page.getTotal());
    }

    SmsChannelDO convertToChannelDO(SmsChannelSaveReqVO bean);

    SmsChannelRespVO convertToChannelRespVO(SmsChannelDO bean);

    List<SmsChannelRespVO> convertChannelRespVOList(List<SmsChannelDO> list);

    List<SmsChannelSimpleRespVO> convertChannelSimpleRespVOList(List<SmsChannelDO> list);

    default PageResult<SmsChannelRespVO> convertChannelRespVOPage(PageResult<SmsChannelDO> page) {
        return new PageResult<>(convertChannelRespVOList(page.getList()), page.getTotal());
    }

    SmsChannelProperties convertToChannelProperties(SmsChannelDO bean);

    SmsLogRespVO convertToLogRespVO(SmsLogDO bean);

    List<SmsLogRespVO> convertLogRespVOList(List<SmsLogDO> list);

    default PageResult<SmsLogRespVO> convertLogRespVOPage(PageResult<SmsLogDO> page) {
        return new PageResult<>(convertLogRespVOList(page.getList()), page.getTotal());
    }

}
