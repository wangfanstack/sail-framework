package cn.sailstack.framework.tenant.core.job;

import cn.sailstack.framework.common.util.exception.ExceptionUtils;
import cn.sailstack.framework.common.util.json.JsonUtils;
import cn.sailstack.framework.tenant.core.service.TenantFrameworkService;
import cn.sailstack.framework.tenant.core.util.TenantUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多租户 JobHandler AOP
 * 任务执行时，会按照租户逐个执行 Job 的逻辑
 *
 * 注意，需要保证 JobHandler 的幂等性。因为 Job 因为某个租户执行失败重试时，之前执行成功的租户也会再次执行。
 *
 * @author 农E链源码
 */
@Aspect
@RequiredArgsConstructor
@Slf4j
public class TenantJobAspect {

    private final TenantFrameworkService tenantFrameworkService;

    @Around("@annotation(tenantJob)")
    public String around(ProceedingJoinPoint joinPoint, TenantJob tenantJob) {
        // 获得租户列表
        List<Long> tenantIds = tenantFrameworkService.getTenantIds();
        if (tenantIds == null || tenantIds.isEmpty()) {
            return null;
        }

        // 逐个租户，执行 Job
        Map<Long, String> results = new ConcurrentHashMap<>();
        tenantIds.parallelStream().forEach(tenantId -> {
            // TODO 芋艿：先通过 parallel 实现并行；1）多个租户，是一条执行日志；2）异常的情况
            TenantUtils.execute(tenantId, () -> {
                try {
                    Object result = joinPoint.proceed();
                    results.put(tenantId, result != null ? result.toString() : "");
                } catch (Throwable e) {
                    log.error("[execute][租户({}) 执行 Job 发生异常", tenantId, e);
                    results.put(tenantId, ExceptionUtils.getRootCauseMessage(e));
                }
            });
        });
        return JsonUtils.toJsonString(results);
    }

}
