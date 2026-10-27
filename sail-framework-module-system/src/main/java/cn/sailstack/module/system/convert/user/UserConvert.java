package cn.sailstack.module.system.convert.user;

import cn.sailstack.framework.common.util.collection.CollectionUtils;
import cn.sailstack.framework.common.util.collection.MapUtils;
import cn.sailstack.module.system.api.user.dto.AdminUserRespDTO;
import cn.sailstack.module.system.controller.admin.auth.vo.AuthRegisterReqVO;
import cn.sailstack.module.system.controller.admin.dept.vo.dept.DeptSimpleRespVO;
import cn.sailstack.module.system.controller.admin.dept.vo.post.PostSimpleRespVO;
import cn.sailstack.module.system.controller.admin.permission.vo.role.RoleSimpleRespVO;
import cn.sailstack.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import cn.sailstack.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import cn.sailstack.module.system.controller.admin.user.vo.user.UserImportExcelVO;
import cn.sailstack.module.system.controller.admin.user.vo.user.UserRespVO;
import cn.sailstack.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import cn.sailstack.module.system.controller.admin.user.vo.user.UserSimpleRespVO;
import cn.sailstack.module.system.controller.admin.oauth2.vo.user.OAuth2UserInfoRespVO;
import cn.sailstack.module.system.controller.admin.oauth2.vo.user.OAuth2UserUpdateReqVO;
import cn.sailstack.module.system.dal.dataobject.dept.DeptDO;
import cn.sailstack.module.system.dal.dataobject.dept.PostDO;
import cn.sailstack.module.system.dal.dataobject.permission.RoleDO;
import cn.sailstack.module.system.dal.dataobject.user.AdminUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    AdminUserRespDTO convertToRespDTO(AdminUserDO bean);

    List<AdminUserRespDTO> convertRespDTOList(List<AdminUserDO> list);

    UserRespVO convert(AdminUserDO bean);

    List<UserRespVO> convertList(List<AdminUserDO> list);

    default List<UserRespVO> convertList(List<AdminUserDO> list, Map<Long, DeptDO> deptMap) {
        return CollectionUtils.convertList(list, user -> convert(user, deptMap.get(user.getDeptId())));
    }

    default UserRespVO convert(AdminUserDO user, DeptDO dept) {
        UserRespVO userVO = convert(user);
        if (dept != null) {
            userVO.setDeptName(dept.getName());
        }
        return userVO;
    }

    UserSimpleRespVO convertToSimpleRespVO(AdminUserDO bean);

    default List<UserSimpleRespVO> convertSimpleList(List<AdminUserDO> list, Map<Long, DeptDO> deptMap) {
        return CollectionUtils.convertList(list, user -> {
            UserSimpleRespVO userVO = convertToSimpleRespVO(user);
            MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> userVO.setDeptName(dept.getName()));
            return userVO;
        });
    }

    DeptSimpleRespVO convert(DeptDO bean);

    PostSimpleRespVO convert(PostDO bean);

    List<PostSimpleRespVO> convertPostList(List<PostDO> list);

    RoleSimpleRespVO convert(RoleDO bean);

    List<RoleSimpleRespVO> convertRoleList(List<RoleDO> list);

    default UserProfileRespVO convert(AdminUserDO user, List<RoleDO> userRoles,
                                      DeptDO dept, List<PostDO> posts) {
        UserProfileRespVO userVO = convertToProfileRespVO(user);
        userVO.setRoles(convertRoleList(userRoles));
        userVO.setDept(convert(dept));
        userVO.setPosts(convertPostList(posts));
        return userVO;
    }

    UserProfileRespVO convertToProfileRespVO(AdminUserDO bean);

    OAuth2UserInfoRespVO convertToOAuth2UserInfo(AdminUserDO bean);

    OAuth2UserInfoRespVO.Dept convertToOAuth2Dept(DeptDO bean);

    List<OAuth2UserInfoRespVO.Post> convertToOAuth2PostList(List<PostDO> list);

    UserProfileUpdateReqVO convertToProfileUpdateReqVO(OAuth2UserUpdateReqVO bean);

    AdminUserDO convertToDO(UserSaveReqVO bean);

    AdminUserDO convertToDO(AuthRegisterReqVO bean);

    AdminUserDO convertToDO(UserProfileUpdateReqVO bean);

    UserSaveReqVO convertToSaveReqVO(AdminUserDO bean);

    UserSaveReqVO convertToSaveReqVOFromImport(UserImportExcelVO bean);

    AdminUserDO convertToDOFromImport(UserImportExcelVO bean);

}
