package io.bhex.bhop.common.grpc.client;

import io.bhex.base.admin.*;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.grpc.client
 * @Author: ming.xu
 * @CreateDate: 09/10/2018 4:55 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public interface AdminRoleAuthClient {

    /**
     * 新建角色，关联权限。可以同步关联用户
     * @param request
     * @return
     */
    SaveRoleReply addRole(AddRoleRequest request);

    /**
     * 更新角色信息
     * @param request
     * @return
     */
    SaveRoleReply updateRole(UpdateRoleRequest request);

    /**
     * 角色信息列表
     * @param request
     * @return
     */
    ListRoleInfoReply listRoleInfo(ListRoleInfoRequest request);

    /**
     * 角色信息列表
     *      如果参数里有传递角色id，则通过enable字段，判断用户是否拥有此权限
     * @param request
     * @return
     */
    ListUserInfoReply listUserInfo(ListUserInfoRequest request);

    /**
     * 获取角色信息
     * @param request
     * @return
     */
    RoleInfo getRoleInfo(GetRoleInfoRequest request);

    /**
     * 更新用户的角色列表
     * @return
     */
    SaveUserRoleReply saveUserRole(SaveUserRoleRequest request);

    /**
     * 角色启禁用
     * @param request
     * @return
     */
    SaveRoleReply enableRole(EnableRoleRequest request);

    /**
     * 获取全部权限列表
     * @param request
     * @return
     */
    ListAuthPathInfoReply listAuthPathInfo(ListAuthPathInfoRequest request);

    /**
     * 通过用户id，获取此用户全部的权限路径，用于登录权限校验
     * @param request
     * @return
     */
    ListAllAuthByUserIdReply listAllAuthByUserId(ListAllAuthByUserIdRequest request);

    /**
     * 通过角色id，获取此用户全部的权限路径，用于登录权限校验
     * @param request
     * @return
     */
    ListAllAuthByRoleIdReply listAllAuthByRoleId(ListAllAuthByRoleIdRequest request);

    /**
     * 通过机构全部权限id，用于划分用户权限。（并限制不同机构拥有不同列表）
     * @param request
     * @return
     */
    ListAllAuthIdReply listAllAuthId(ListAllAuthIdRequest request);
}
