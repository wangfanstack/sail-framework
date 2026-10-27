package cn.sailstack.module.system.convert.mail;

import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.system.controller.admin.mail.vo.account.MailAccountRespVO;
import cn.sailstack.module.system.controller.admin.mail.vo.account.MailAccountSaveReqVO;
import cn.sailstack.module.system.controller.admin.mail.vo.account.MailAccountSimpleRespVO;
import cn.sailstack.module.system.controller.admin.mail.vo.log.MailLogRespVO;
import cn.sailstack.module.system.controller.admin.mail.vo.template.MailTemplateRespVO;
import cn.sailstack.module.system.controller.admin.mail.vo.template.MailTemplateSaveReqVO;
import cn.sailstack.module.system.controller.admin.mail.vo.template.MailTemplateSimpleRespVO;
import cn.sailstack.module.system.dal.dataobject.mail.MailAccountDO;
import cn.sailstack.module.system.dal.dataobject.mail.MailLogDO;
import cn.sailstack.module.system.dal.dataobject.mail.MailTemplateDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MailConvert {

    MailConvert INSTANCE = Mappers.getMapper(MailConvert.class);

    MailAccountRespVO convertToAccountRespVO(MailAccountDO bean);

    List<MailAccountRespVO> convertAccountRespVOList(List<MailAccountDO> list);

    default PageResult<MailAccountRespVO> convertAccountRespVOPage(PageResult<MailAccountDO> page) {
        return new PageResult<>(convertAccountRespVOList(page.getList()), page.getTotal());
    }

    List<MailAccountSimpleRespVO> convertAccountSimpleRespVOList(List<MailAccountDO> list);

    MailAccountDO convertToAccountDO(MailAccountSaveReqVO bean);

    MailTemplateRespVO convertToTemplateRespVO(MailTemplateDO bean);

    List<MailTemplateRespVO> convertTemplateRespVOList(List<MailTemplateDO> list);

    default PageResult<MailTemplateRespVO> convertTemplateRespVOPage(PageResult<MailTemplateDO> page) {
        return new PageResult<>(convertTemplateRespVOList(page.getList()), page.getTotal());
    }

    List<MailTemplateSimpleRespVO> convertTemplateSimpleRespVOList(List<MailTemplateDO> list);

    MailTemplateDO convertToTemplateDO(MailTemplateSaveReqVO bean);

    MailLogRespVO convertToLogRespVO(MailLogDO bean);

    List<MailLogRespVO> convertLogRespVOList(List<MailLogDO> list);

    default PageResult<MailLogRespVO> convertLogRespVOPage(PageResult<MailLogDO> page) {
        return new PageResult<>(convertLogRespVOList(page.getList()), page.getTotal());
    }

}
