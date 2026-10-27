package cn.sailstack.module.system.api.user;

import cn.sailstack.framework.datapermission.core.annotation.DataPermission;
import cn.sailstack.framework.datapermission.core.util.DataPermissionUtils;
import cn.sailstack.module.system.api.user.dto.AdminUserRespDTO;
import cn.sailstack.module.system.convert.user.UserConvert;
import cn.sailstack.module.system.dal.dataobject.dept.DeptDO;
import cn.sailstack.module.system.dal.dataobject.user.AdminUserDO;
import cn.sailstack.module.system.service.dept.DeptService;
import cn.sailstack.module.system.service.user.AdminUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.sailstack.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * Admin 用户 API 实现类
 *
 * @author 农E链源码
 */
@Service
public class AdminUserApiImpl implements AdminUserApi {

    @Resource
    private AdminUserService userService;
    @Resource
    private DeptService deptService;

    @Override
    @DataPermission(enable = false) // 忽略数据权限，避免因为过滤，导致无法查询用户。类似：https://github.com/YunaiV/ruoyi-vue-pro/issues/1051
    public AdminUserRespDTO getUser(Long id) {
        AdminUserDO user = userService.getUser(id);
        return UserConvert.INSTANCE.convertToRespDTO(user);
    }

    @Override
    @DataPermission(enable = false) // 忽略数据权限，指定手机号的 API 查询用于跨模块数据拼接
    public AdminUserRespDTO getUserByMobile(String mobile) {
        AdminUserDO user = userService.getUserByMobile(mobile);
        return UserConvert.INSTANCE.convertToRespDTO(user);
    }

    @Override
    public List<AdminUserRespDTO> getUserListBySubordinate(Long id) {
        // 1.1 获取用户负责的部门
        List<DeptDO> depts = deptService.getDeptListByLeaderUserId(id);
        if (depts == null || depts.isEmpty()) {
            return Collections.emptyList();
        }
        // 1.2 获取所有子部门
        Set<Long> deptIds = convertSet(depts, DeptDO::getId);
        List<DeptDO> childDeptList = deptService.getChildDeptList(deptIds);
        if (childDeptList != null && !childDeptList.isEmpty()) {
            deptIds.addAll(convertSet(childDeptList, DeptDO::getId));
        }

        // 2. 获取部门对应的用户信息
        List<AdminUserDO> users = userService.getUserListByDeptIds(deptIds);
        users.removeIf(item -> Objects.equals(item.getId(), id)); // 排除自己
        return UserConvert.INSTANCE.convertRespDTOList(users);
    }

    @Override
    public List<AdminUserRespDTO> getUserList(Collection<Long> ids) {
        return DataPermissionUtils.executeIgnore(() -> { // 禁用数据权限。原因是，一般基于指定 id 的 API 查询，都是数据拼接为主
            List<AdminUserDO> users = userService.getUserList(ids);
            return UserConvert.INSTANCE.convertRespDTOList(users);
        });
    }

    @Override
    public List<AdminUserRespDTO> getUserListByDeptIds(Collection<Long> deptIds) {
        List<AdminUserDO> users = userService.getUserListByDeptIds(deptIds);
        return UserConvert.INSTANCE.convertRespDTOList(users);
    }

    @Override
    public List<AdminUserRespDTO> getUserListByPostIds(Collection<Long> postIds) {
        List<AdminUserDO> users = userService.getUserListByPostIds(postIds);
        return UserConvert.INSTANCE.convertRespDTOList(users);
    }

    @Override
    public List<AdminUserRespDTO> getUserListByNickname(String nickname) {
        List<AdminUserDO> users = userService.getUserListByNickname(nickname);
        return UserConvert.INSTANCE.convertRespDTOList(users);
    }

    @Override
    public List<AdminUserRespDTO> getUserListByStatus(Integer status) {
        List<AdminUserDO> users = userService.getUserListByStatus(status);
        return UserConvert.INSTANCE.convertRespDTOList(users);
    }

    @Override
    public AdminUserRespDTO validateUser(Long id) {
        AdminUserDO user = userService.validateUser(id);
        return UserConvert.INSTANCE.convertToRespDTO(user);
    }

    @Override
    public void validateUserList(Collection<Long> ids) {
        userService.validateUserList(ids);
    }

}
