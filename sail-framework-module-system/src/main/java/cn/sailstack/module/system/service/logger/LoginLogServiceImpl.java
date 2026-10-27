package cn.sailstack.module.system.service.logger;

import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.sailstack.module.system.controller.admin.logger.vo.loginlog.LoginLogPageReqVO;
import cn.sailstack.module.system.convert.logger.OperateLogConvert;
import cn.sailstack.module.system.dal.dataobject.logger.LoginLogDO;
import cn.sailstack.module.system.dal.mysql.logger.LoginLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 登录日志 Service 实现
 */
@Service
@Validated
public class LoginLogServiceImpl implements LoginLogService {

    @Resource
    private LoginLogMapper loginLogMapper;

    @Override
    public LoginLogDO getLoginLog(Long id) {
        return loginLogMapper.selectById(id);
    }

    @Override
    public PageResult<LoginLogDO> getLoginLogPage(LoginLogPageReqVO pageReqVO) {
        return loginLogMapper.selectPage(pageReqVO);
    }

    @Override
    public void createLoginLog(LoginLogCreateReqDTO reqDTO) {
        LoginLogDO loginLog = OperateLogConvert.INSTANCE.convertToLoginLogDO(reqDTO);
        loginLogMapper.insert(loginLog);
    }

}
