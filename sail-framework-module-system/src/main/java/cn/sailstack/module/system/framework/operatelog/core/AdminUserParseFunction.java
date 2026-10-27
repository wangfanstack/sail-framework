package cn.sailstack.module.system.framework.operatelog.core;

import cn.sailstack.module.system.dal.dataobject.user.AdminUserDO;
import cn.sailstack.module.system.service.user.AdminUserService;
import com.mzt.logapi.service.IParseFunction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 管理员名字的 {@link IParseFunction} 实现类
 *
 * @author HUIHUI
 */
@Slf4j
@Component
public class AdminUserParseFunction implements IParseFunction {

    public static final String NAME = "getAdminUserById";

    @Resource
    private AdminUserService adminUserService;

    @Override
    public String functionName() {
        return NAME;
    }

    @Override
    public String apply(Object value) {
        if (isEmptyIfStr(value)) {
            return "";
        }

        // 获取用户信息
        AdminUserDO user = adminUserService.getUser(Long.valueOf(value.toString()));
        if (user == null) {
            log.warn("[apply][获取用户{{}}为空", value);
            return "";
        }
        // 返回格式 农E链源码(13888888888)
        String nickname = user.getNickname();
        if (StringUtils.isEmpty(user.getMobile())) {
            return nickname;
        }
        return String.format("%s(%s)", nickname, user.getMobile());
    }

    static boolean isEmptyIfStr(Object value) {
        return value == null || (value instanceof CharSequence cs && cs.length() == 0);
    }

}
