package io.bhex.bhop.common.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.bhex.base.admin.*;
import io.bhex.bhop.common.dto.AuthInfoDTO;
import io.bhex.bhop.common.dto.AuthPathInfoDTO;
import io.bhex.bhop.common.dto.RoleInfoDTO;
import io.bhex.bhop.common.dto.UserInfoDTO;
import io.bhex.bhop.common.dto.param.*;
import io.bhex.bhop.common.grpc.client.AdminRoleAuthClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service
 * @Author: ming.xu
 * @CreateDate: 10/12/2018 6:16 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Service
public class AdminRoleAuthService {

    @Autowired
    private AdminRoleAuthClient adminRoleAuthClient;

    /**
     * 新建角色，关联权限。可以同步关联用户
     * @param param
     * @return
     */
    public Boolean addRole(AddRolePO param) {
        AddRoleRequest.Builder builder = AddRoleRequest.newBuilder();
        builder.addAllUserIds(param.getUserIds())
                .setName(param.getName())
                .setOrgId(param.getOrgId());
        Map<Long, Integer> authIdMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(param.getAuthPathIds())) {
            param.getAuthPathIds().forEach(a -> {
                authIdMap.put(a, 2);
            });
            builder.putAllAuthIdMap(authIdMap);
        } else if (!CollectionUtils.isEmpty(param.getAuthPaths())) {
            authIdMap.putAll(param.getAuthPaths());
            builder.putAllAuthIdMap(authIdMap);
        }
        SaveRoleReply reply = adminRoleAuthClient.addRole(builder.build());
        return reply.getResult();
    }

    /**
     * 更新角色信息
     * @param param
     * @return
     */
    public Boolean updateRole(UpdateRolePO param) {
        UpdateRoleRequest.Builder builder = UpdateRoleRequest.newBuilder();
        builder.addAllUserIds(param.getUserIds())
                .setName(param.getName())
                .setOrgId(param.getOrgId())
                .setRoleId(param.getRoleId());
        Map<Long, Integer> authIdMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(param.getAuthPathIds())) {
            param.getAuthPathIds().forEach(a -> {
                authIdMap.put(a, 2);
            });
            builder.putAllAuthIdMap(authIdMap);
        } else if (!CollectionUtils.isEmpty(param.getAuthPaths())) {
            authIdMap.putAll(param.getAuthPaths());
            builder.putAllAuthIdMap(authIdMap);
        }

        SaveRoleReply reply = adminRoleAuthClient.updateRole(builder.build());
        return reply.getResult();
    }

    /**
     * 角色信息列表
     * @param param
     * @return
     */
    public List<RoleInfoDTO> listRoleInfo(ListRoleInfoPO param) {
        ListRoleInfoRequest request = ListRoleInfoRequest.newBuilder()
                .setOrgId(param.getOrgId())
                .setAdminId(param.getAdminId())
                .build();

        ListRoleInfoReply reply = adminRoleAuthClient.listRoleInfo(request);
        List<RoleInfoDTO> result = new ArrayList();
        reply.getRoleInfoList().forEach(roleInfo -> {
            RoleInfoDTO dto = new RoleInfoDTO();
            BeanUtils.copyProperties(roleInfo, dto);
            result.add(dto);
        });
        return result;
    }

    /**
     * 用户信息列表
     *      如果参数里有传递角色id，则通过enable字段，判断用户是否拥有此权限
     * @param param
     * @return
     */
    public List<UserInfoDTO> listUserInfo(ListUserInfoPO param) {
        if (Objects.isNull(param.getRoleId())) {
            param.setRoleId(0L);
        }
        ListUserInfoRequest request = ListUserInfoRequest.newBuilder()
                .setOrgId(param.getOrgId())
                .setRoleId(param.getRoleId())
                .build();

        ListUserInfoReply reply = adminRoleAuthClient.listUserInfo(request);
        List<UserInfoDTO> result = new ArrayList();
        reply.getUserInfosList().forEach(userInfo -> {
            UserInfoDTO dto = new UserInfoDTO();
            BeanUtils.copyProperties(userInfo, dto);
            dto.setRoleNameList(userInfo.getRoleNameListList());
            dto.setUsername(userInfo.getRealName());
            result.add(dto);
        });
        return result;
    }

    /**
     * 获取角色信息
     * @param param
     * @return
     */
    public RoleInfoDTO getRoleInfo(GetRoleInfoPO param) {
        GetRoleInfoRequest request = GetRoleInfoRequest.newBuilder()
                .setOrgId(param.getOrgId())
                .setRoleId(param.getRoleId())
                .build();

        RoleInfo roleInfo = adminRoleAuthClient.getRoleInfo(request);
        RoleInfoDTO dto = new RoleInfoDTO();
        BeanUtils.copyProperties(roleInfo, dto);
        List<Long> authIds = listAllAuthIdByRoleId(param.getOrgId(), param.getRoleId());
        dto.setAuthPathIds(authIds);
        return dto;
    }

    /**
     * 更新用户的角色列表
     * @return
     */
    public Boolean saveUserRole(SaveUserRolePO param) {
        SaveUserRoleRequest request = SaveUserRoleRequest.newBuilder()
                .addAllRoleIds(param.getRoleIds())
                .setOrgId(param.getOrgId())
                .setUserId(param.getUserId())
                .build();

        SaveUserRoleReply reply = adminRoleAuthClient.saveUserRole(request);
        return reply.getResult();
    }

    /**
     * 角色启禁用
     * @param param
     * @return
     */
    public Boolean enableRole(EnableRolePO param) {
        EnableRoleRequest request = EnableRoleRequest.newBuilder()
                .setOrgId(param.getOrgId())
                .setRoleId(param.getRoleId())
                .setStatus(param.getStatus() == 1? 1: 2)
                .build();

        SaveRoleReply reply = adminRoleAuthClient.enableRole(request);
        return reply.getResult();
    }

    /**
     * 获取全部权限列表
     * @param param
     * @return
     */
    public List<AuthPathInfoDTO> listAuthPathInfo(ListAuthPathInfoPO param) {
        ListAuthPathInfoRequest request = ListAuthPathInfoRequest.newBuilder()
                .setOrgId(param.getOrgId())
                .setRoleId(param.getRoleId())
                .build();

        ListAuthPathInfoReply reply = adminRoleAuthClient.listAuthPathInfo(request);
        List<AuthPathInfoDTO> result = new ArrayList();
        reply.getAuthInfosList().forEach(userInfo -> {
            AuthPathInfoDTO dto = new AuthPathInfoDTO();
            BeanUtils.copyProperties(userInfo, dto);
            subInfoDeepCopy(userInfo, dto);
            result.add(dto);
        });
        return result;
    }

    private void subInfoDeepCopy(AuthPathInfo info, AuthPathInfoDTO dto) {
        List<AuthPathInfo> subAuthInfosList = info.getSubAuthInfosList();
        if (!CollectionUtils.isEmpty(subAuthInfosList)) {
            List<AuthPathInfoDTO> subDtoList = subAuthInfosList.stream().map(subInfo -> {
                AuthPathInfoDTO subDto = new AuthPathInfoDTO();
                BeanUtils.copyProperties(subInfo, subDto);
                subInfoDeepCopy(subInfo, subDto);
                return subDto;
            }).collect(Collectors.toList());
            dto.setSubAuthInfos(subDtoList);
        }
    }

    /**
     * 通过用户id，获取此用户全部的权限路径Id，用于前端展示菜单列表
     * @param param
     * @return
     */
    public List<Long> listAllAuthIdByUserId(ListAllAuthByUserIdPO param) {
        List<AuthInfoDTO> dtoList = listAllAuthByUserId(param);
        List<Long> authIds = new ArrayList<>();
        dtoList.forEach(dto -> {
            authIds.add(dto.getAuthId());
        });
        return authIds;
    }

    /**
     * 通过用户id，获取此用户全部的权限路径Id，用于前端展示菜单列表 包含菜单的读写权限
     * @param param
     * @return
     */
    public Map<Long, Integer> listAllAuthIdByUserId2(ListAllAuthByUserIdPO param) {
        List<AuthInfoDTO> dtoList = listAllAuthByUserId(param);
        Map<Long, Integer> authIds = Maps.newHashMap();
        dtoList.forEach(dto -> {
            authIds.put(dto.getAuthId(), dto.getEditAbleStatus());
        });
        return authIds;
    }

    /**
     * 通过用户id，获取此用户全部的权限路径，用于登录权限校验
     * @param param
     * @return
     */
    public List<AuthInfoDTO> listAllAuthByUserId(ListAllAuthByUserIdPO param) {
        ListAllAuthByUserIdReply reply = listAllAuthByUserId(param.getOrgId(), param.getUserId());
        List<AuthInfoDTO> result = new ArrayList<>();
        reply.getAuthPathInfosList().forEach(authInfo -> {
            AuthInfoDTO dto = new AuthInfoDTO();
            BeanUtils.copyProperties(authInfo, dto);
            result.add(dto);
        });
        return result;
    }

    /**
     * 通过机构全部权限id，用于划分用户权限。（并限制不同机构拥有不同列表）
     * @param orgId
     * @return
     */
    public List<Long> listAllAuthId(Long orgId) {
        ListAllAuthIdRequest request = ListAllAuthIdRequest.newBuilder()
                .setOrgId(orgId)
                .build();
        ListAllAuthIdReply reply = adminRoleAuthClient.listAllAuthId(request);
        return reply.getAuthIdsList();
    }

    public ListAllAuthByUserIdReply listAllAuthByUserId(Long orgId, Long userId) {
        ListAllAuthByUserIdRequest request = ListAllAuthByUserIdRequest.newBuilder()
                .setOrgId(orgId)
                .setUserId(userId)
                .build();

        ListAllAuthByUserIdReply reply = adminRoleAuthClient.listAllAuthByUserId(request);
        return reply;
    }

    public List<Long> listAllAuthIdByRoleId(Long orgId, Long roleId) {
        ListAllAuthByRoleIdReply listAllAuthByRoleIdReply = listAllAuthByRoleId(orgId, roleId);
        List<AuthInfo> authPathInfos = listAllAuthByRoleIdReply.getAuthPathInfosList();
        List<Long> authIds = new ArrayList<>();
        authPathInfos.forEach(info -> {
            authIds.add(info.getAuthId());
        });
        return authIds;
    }

    public Map<Long, Integer> listAllAuthIdByRoleIdV1(Long orgId, Long roleId) {
        ListAllAuthByRoleIdReply listAllAuthByRoleIdReply = listAllAuthByRoleId(orgId, roleId);
        List<AuthInfo> authPathInfos = listAllAuthByRoleIdReply.getAuthPathInfosList();
        Map<Long, Integer> authIds = Maps.newHashMap();
        authPathInfos.forEach(info -> {
            authIds.put(info.getAuthId(), info.getEditAbleStatus());
        });
        return authIds;
    }

    public ListAllAuthByRoleIdReply listAllAuthByRoleId(Long orgId, Long roleId) {
        ListAllAuthByRoleIdRequest request = ListAllAuthByRoleIdRequest.newBuilder()
                .setOrgId(orgId)
                .setRoleId(roleId)
                .build();

        ListAllAuthByRoleIdReply reply = adminRoleAuthClient.listAllAuthByRoleId(request);
        return reply;
    }
}
