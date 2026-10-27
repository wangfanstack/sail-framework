package cn.sailstack.module.system.controller.admin.mail;

import cn.sailstack.framework.common.enums.CommonStatusEnum;
import cn.sailstack.framework.common.pojo.CommonResult;
import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.system.controller.admin.mail.vo.template.*;
import cn.sailstack.module.system.convert.mail.MailConvert;
import cn.sailstack.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.sailstack.module.system.service.mail.MailSendService;
import cn.sailstack.module.system.service.mail.MailTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.sailstack.framework.common.pojo.CommonResult.success;
import static cn.sailstack.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 邮件模版")
@RestController
@RequestMapping("/system/mail-template")
public class MailTemplateController {

    @Resource
    private MailTemplateService mailTempleService;
    @Resource
    private MailSendService mailSendService;

    @PostMapping("/create")
    @Operation(summary = "创建邮件模版")
    @PreAuthorize("@ss.hasPermission('system:mail-template:create')")
    public CommonResult<Long> createMailTemplate(@Valid @RequestBody MailTemplateSaveReqVO createReqVO){
        return success(mailTempleService.createMailTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改邮件模版")
    @PreAuthorize("@ss.hasPermission('system:mail-template:update')")
    public CommonResult<Boolean> updateMailTemplate(@Valid @RequestBody MailTemplateSaveReqVO updateReqVO){
        mailTempleService.updateMailTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除邮件模版")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:mail-template:delete')")
    public CommonResult<Boolean> deleteMailTemplate(@RequestParam("id") Long id) {
        mailTempleService.deleteMailTemplate(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除邮件模版")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('system:mail-template:delete')")
    public CommonResult<Boolean> deleteMailTemplateList(@RequestParam("ids") List<Long> ids) {
        mailTempleService.deleteMailTemplateList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得邮件模版")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:mail-template:query')")
    public CommonResult<MailTemplateRespVO> getMailTemplate(@RequestParam("id") Long id) {
        MailTemplateDO template = mailTempleService.getMailTemplate(id);
        return success(MailConvert.INSTANCE.convertToTemplateRespVO(template));
    }

    @GetMapping("/page")
    @Operation(summary = "获得邮件模版分页")
    @PreAuthorize("@ss.hasPermission('system:mail-template:query')")
    public CommonResult<PageResult<MailTemplateRespVO>> getMailTemplatePage(@Valid MailTemplatePageReqVO pageReqVO) {
        PageResult<MailTemplateDO> pageResult = mailTempleService.getMailTemplatePage(pageReqVO);
        return success(MailConvert.INSTANCE.convertTemplateRespVOPage(pageResult));
    }

    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "获得邮件模版精简列表", description = "只包含被开启的邮件模版，主要用于前端的下拉选项")
    public CommonResult<List<MailTemplateSimpleRespVO>> getSimpleMailTemplateList() {
        List<MailTemplateDO> list = mailTempleService.getMailTemplateListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        return success(MailConvert.INSTANCE.convertTemplateSimpleRespVOList(list));
    }

    @PostMapping("/send-mail")
    @Operation(summary = "发送短信")
    @PreAuthorize("@ss.hasPermission('system:mail-template:send-mail')")
    public CommonResult<Long> sendMail(@Valid @RequestBody MailTemplateSendReqVO sendReqVO) {
        return success(mailSendService.sendSingleMailToAdmin(getLoginUserId(),
                sendReqVO.getToMails(), sendReqVO.getCcMails(), sendReqVO.getBccMails(),
                sendReqVO.getTemplateCode(), sendReqVO.getTemplateParams()));
    }

}
