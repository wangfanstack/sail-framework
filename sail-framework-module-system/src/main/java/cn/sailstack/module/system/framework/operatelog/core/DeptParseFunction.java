package cn.sailstack.module.system.framework.operatelog.core;

import cn.sailstack.module.system.dal.dataobject.dept.DeptDO;
import cn.sailstack.module.system.service.dept.DeptService;
import com.mzt.logapi.service.IParseFunction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 部门名字的 {@link IParseFunction} 实现类
 *
 * @author HUIHUI
 */
@Slf4j
@Component
public class DeptParseFunction implements IParseFunction {

    public static final String NAME = "getDeptById";

    @Resource
    private DeptService deptService;

    @Override
    public String functionName() {
        return NAME;
    }

    @Override
    public String apply(Object value) {
        if (AdminUserParseFunction.isEmptyIfStr(value)) {
            return "";
        }

        // 获取部门信息
        DeptDO dept = deptService.getDept(Long.valueOf(value.toString()));
        if (dept == null) {
            log.warn("[apply][获取部门{{}}为空", value);
            return "";
        }
        return dept.getName();
    }

}
