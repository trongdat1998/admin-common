package io.bhex.bhop.common.service.impl;

import com.google.common.collect.Lists;
import com.google.protobuf.TextFormat;
import io.bhex.base.admin.*;
import io.bhex.bhop.common.constants.StatusConstant;
import io.bhex.bhop.common.entity.*;
import io.bhex.bhop.common.mapper.*;
import io.bhex.bhop.common.service.AdminUserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service.impl
 * @Author: ming.xu
 * @CreateDate: 06/10/2018 3:58 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class AdminUserAuthServiceImpl implements AdminUserAuthService {

    @Autowired
    private AdminUserMapper userMapper;

    @Autowired
    private AdminAuthMapper authMapper;

    @Autowired
    private AdminRoleMapper roleMapper;

    @Autowired
    private AdminAuthDetailMapper authDetailMapper;

    @Autowired
    private RoleAuthIndexMapper roleAuthIndexMapper;

    @Autowired
    private UserRoleIndexMapper userRoleIndexMapper;

    @Transactional(rollbackFor=Exception.class)
    @Override
    public SaveRoleReply addRole(AddRoleRequest request) {

        AdminRole role=findRole(request.getOrgId(),request.getName());
        if(Objects.nonNull(role)){

            SaveRoleReply reply = SaveRoleReply.newBuilder()
                    .setResult(true)
                    .build();
            return reply;
        }

        //save role
        AdminRole adminRole = new AdminRole();
        adminRole.setName(request.getName());
        adminRole.setOrgId(request.getOrgId());
        adminRole.setEnable(0);
        adminRole.setStatus(StatusConstant.PASS_STATUS);
        adminRole.setCreatedAt(System.currentTimeMillis());
        roleMapper.insert(adminRole);
        insertRoleAuthIndex(request.getAuthIdMapMap(), adminRole.getId(), request.getOrgId());
        insertUserRoleIndexByRoleId(request.getUserIdsList(), adminRole.getId(), request.getOrgId());
        SaveRoleReply reply = SaveRoleReply.newBuilder()
                .setResult(true)
                .build();
        return reply;
    }

    private AdminRole findRole(long orgId, String name) {

        Example exp=new Example(AdminRole.class);
        exp.createCriteria().andEqualTo("orgId",orgId).andEqualTo("name", name);

        List<AdminRole> list=roleMapper.selectByExample(exp);
        if(CollectionUtils.isEmpty(list)){
            return null;
        }

        return list.stream().findAny().get();

    }

    @Transactional(rollbackFor=Exception.class)
    @Override
    public SaveRoleReply updateRole(UpdateRoleRequest request) {
        log.info("UpdateRoleRequest:{}", TextFormat.shortDebugString(request));
        //检查是否存在同名角色
        AdminRole role=findRole(request.getOrgId(),request.getName());
        if(Objects.nonNull(role) && request.getRoleId()!=role.getId().longValue()){

            SaveRoleReply reply = SaveRoleReply.newBuilder()
                    .setResult(false)
                    .build();
            return reply;
        }

        SaveRoleReply.Builder builder = SaveRoleReply.newBuilder();
        AdminRole adminRole = roleMapper.selectByPrimaryKey(request.getRoleId());
        builder.setResult(false);
        if (null != adminRole && adminRole.getOrgId().equals(request.getOrgId())) {
            adminRole.setName(request.getName());
            roleMapper.updateByPrimaryKey(adminRole);
            updateRoleAuthIndex(request.getAuthIdMapMap(), request.getRoleId(), request.getOrgId());
            updateUserRoleIndexByRoleId(request.getUserIdsList(), request.getRoleId(), request.getOrgId());
            builder.setResult(true);
        }
        return builder.build();
    }

    @Override
    public ListRoleInfoReply listRoleInfo(ListRoleInfoRequest request) {
        ListRoleInfoReply.Builder builder = ListRoleInfoReply.newBuilder();
        List<RoleInfo> roleInfoList = listRoleInfoByOrgId(request.getOrgId(), request.getAdminId(),true);
        builder.addAllRoleInfo(roleInfoList);

        return builder.build();
    }

    @Override
    public RoleInfo getRoleInfo(GetRoleInfoRequest request) {
        AdminRole adminRole = roleMapper.selectByPrimaryKey(request.getRoleId());
        if (null != adminRole && adminRole.getOrgId().equals(request.getOrgId())) {
            RoleInfo.Builder builder = RoleInfo.newBuilder();
            BeanUtils.copyProperties(adminRole, builder);
            builder.setRoleId(adminRole.getId());
            return builder.build();
        }
        return null;
    }

    @Override
    public ListUserInfoReply listUserInfo(ListUserInfoRequest request) {
        ListUserInfoReply.Builder replyBuilder = ListUserInfoReply.newBuilder();
        List<AdminUser> adminUserList = listSubAdminUserByOrgId(request.getOrgId());

        if (!CollectionUtils.isEmpty(adminUserList)) {
            List<UserInfo> userInfos = new ArrayList<>();
            for (AdminUser user: adminUserList) {
                UserInfo.Builder builder = transformUser(user);
                //BeanUtils.copyProperties(user, builder);
                //如果参数里有传递角色id，则通过enable字段，判断用户是否拥有此权限
                List<AdminRole> adminRoles = listRoleInfoByUserId(user.getId(), user.getOrgId(), false);
                if (!CollectionUtils.isEmpty(adminRoles)) {
                    List<Long> roleIds = new ArrayList();
                    List<String> roleNameList = new ArrayList();

                    for (AdminRole adminRole : adminRoles) {
                        roleIds.add(adminRole.getId());
                        roleNameList.add(adminRole.getName());
                    }
                    builder.addAllRoleNameList(roleNameList);
                    builder.addAllRoleIds(roleIds);
                    if (request.getRoleId() != 0L) {
                        //判断用户是否拥有此角色
                        if (roleIds.contains(request.getRoleId())) {
                            builder.setEnable(true);
                        }
                    }
                } else {
                    builder.setEnable(false);
                }
                userInfos.add(builder.build());
            }

            replyBuilder.addAllUserInfos(userInfos);
        }
        return replyBuilder.build();
    }

    private UserInfo.Builder transformUser(AdminUser user){

       return UserInfo.newBuilder()
                .setEnable(user.getDeleted().intValue()==1?true:false)
                .setCreatedAt(user.getCreatedAt().getTime())
                .setDeleted(user.getDeleted())
                .setId(user.getId())
                .setOrgId(user.getOrgId())
                .setSaasOrgId(user.getSaasOrgId())
                .setStatus(user.getStatus())
                .setAreaCode(user.getAreaCode())
                .setCreatedIp(user.getCreatedIp())
                .setEmail(user.getEmail())
                .setOrgName(user.getOrgName())
                .setPosition(user.getPosition())
                .setUsername(user.getUsername())
                .setRealName(user.getRealName());

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean saveUserRole(Long orgId, Long userId, List<Long> roleIds) {
        updateUserRoleIndexByUserId(roleIds, userId, orgId);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SaveRoleReply enableRole(EnableRoleRequest request) {
        AdminRole adminRole = roleMapper.selectByPrimaryKey(request.getRoleId());
        if (null != adminRole && adminRole.getOrgId().equals(request.getOrgId())) {
            //adminRole.setStatus(request.getStatus());
            AdminRole tmp=new AdminRole();
            tmp.setStatus(request.getStatus());
            tmp.setId(request.getRoleId());

            int rows=roleMapper.updateByPrimaryKeySelective(tmp);
            if(rows==0){
                log.error("Update role fail...,roleId={},orgId={}",request.getRoleId(),request.getOrgId());
                throw new IllegalStateException("Enable role fail...");
            }

            Example exp=new Example(UserRoleIndex.class);
            exp.createCriteria().andEqualTo("orgId",request.getOrgId())
                    .andEqualTo("roleId",request.getRoleId());

            UserRoleIndex uri=new UserRoleIndex();
            uri.setStatus(request.getStatus());

            rows = userRoleIndexMapper.updateByExampleSelective(uri,exp);
            if(rows == 0){
                //log.error("Update user role index fail...,roleId={},orgId={}",request.getRoleId(),request.getOrgId());
                //throw new IllegalStateException("Enable role fail...");
            }

            SaveRoleReply.Builder builder = SaveRoleReply.newBuilder();
            builder.setResult(true);
            return builder.build();
        }
        return null;
    }

    @Override
    public ListAuthPathInfoReply listAuthPathInfo(ListAuthPathInfoRequest request) {
        List<AdminAuth> adminAuths = listAuth(request.getOrgId());
        Map<Long, String> authNameByLocale = mapAuthNameByLocale(request.getLocale());
        //key = authId
        List<AuthPathInfo> level1AuthList = new ArrayList<>();
        //key = parendAuthId
        Map<Long, List<AuthPathInfo>> level2AuthMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(adminAuths)) {
            List<RoleAuthIndex> roleAuthIndexList = listRoleAuthIndex(request.getOrgId(), request.getRoleId());
            Map<Long, RoleAuthIndex> roleAuthIndexMap = roleAuthIndexList.stream().collect(Collectors.toMap(RoleAuthIndex::getAuthId, Function.identity()));
            for (AdminAuth auth: adminAuths) {
                AuthPathInfo.Builder builder = AuthPathInfo.newBuilder();
                BeanUtils.copyProperties(auth, builder);
                builder.setAuthId(auth.getId());
                String authName = authNameByLocale.get(auth.getId());
                if (StringUtils.isNoneEmpty(authName)) {
                    builder.setName(authName);
                }
                //判断角色是否拥有此权限
                if (roleAuthIndexMap.containsKey(auth.getId())) {
                    builder.setEnable(true);
                } else {
                    builder.setEnable(false);
                }
                if (auth.getLevel() == 1) {
                    level1AuthList.add(builder.build());
                } else if (auth.getLevel() == 2) {
                    if (level2AuthMap.containsKey(auth.getParentId())) {
                        level2AuthMap.get(auth.getParentId()).add(builder.build());
                    } else {
                        List<AuthPathInfo> list = new ArrayList<>();
                        list.add(builder.build());
                        level2AuthMap.put(auth.getParentId(), list);
                    }
                }
            }
            List<AuthPathInfo> result = level1AuthList.stream().map(auth -> {
                List<AuthPathInfo> authInfos = level2AuthMap.get(auth.getAuthId());
                if (!CollectionUtils.isEmpty(authInfos)) {
                    auth = auth.toBuilder().addAllSubAuthInfos(authInfos).build();
                }
                return auth;
            }).collect(Collectors.toList());
            ListAuthPathInfoReply reply = ListAuthPathInfoReply.newBuilder()
                    .addAllAuthInfos(result)
                    .build();
            return reply;
        }

        return null;
    }

    @Override
    public ListAllAuthByUserIdReply listAllAuthByUserId(ListAllAuthByUserIdRequest request) {
        AdminUser admin = findAdminUserByUserId(request.getUserId());
        List<AuthInfo> authInfos = Lists.newArrayList();
        if (Objects.nonNull(admin) && admin.getAccountType().equals(AdminUser.ROOT_ACCOUNT)) {
            List<AdminAuth> adminAuths = listAllAuth();
            authInfos = adminAuths.stream().map(a -> transform(a, Lists.newArrayList(), true)).collect(Collectors.toList());
        } else {
            List<RoleAuthIndex> roleAuthIndices = listAuthIndexByUserId(request.getUserId(), request.getOrgId());
            List<AdminAuth> adminAuths = listAuthInfoByRoleAuthIndex(roleAuthIndices);
            authInfos = adminAuths.stream().map(a -> transform(a, roleAuthIndices, false)).collect(Collectors.toList());
        }

        ListAllAuthByUserIdReply reply = ListAllAuthByUserIdReply.newBuilder()
                .addAllAuthPathInfos(authInfos)
                .build();

        return reply;
    }

    private AdminUser findAdminUserByUserId(long userId) {

        Example example = new Example(AdminUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", userId);
        return userMapper.selectOneByExample(example);
    }

    private AuthInfo transform(AdminAuth auth, List<RoleAuthIndex> roleAuthIndexList, boolean rootUser){

        AuthInfo.Builder builder = AuthInfo.newBuilder();
        builder.setAuthId(auth.getId());
        builder.setFrontEndPath(auth.getFrontEndPath());
        builder.setPath(auth.getPath());
        if (rootUser) {
            builder.setEditAbleStatus(2);
        } else {
            List<RoleAuthIndex> list = roleAuthIndexList.stream().filter(r -> r.getAuthId().equals(auth.getId())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(list)) {
                boolean editAble = list.stream().anyMatch(r -> r.getEditStatus() == 2);
                if (editAble) {
                    builder.setEditAbleStatus(2);
                } else {
                    builder.setEditAbleStatus(list.get(0).getEditStatus());
                }
                //builder.setEditAbleStatus(optional.get().getEditStatus());
            } else {
                builder.setEditAbleStatus(1);
            }
        }

        return builder.build();

    }

    @Override
    public ListAuthPathIdReply listAuthPathId(ListAuthPathIdRequest request) {
        List<RoleAuthIndex> roleAuthIndices = listAuthIndexByRoleId(request.getOrgId(), Arrays.asList(request.getRoleId()));
        List<AdminAuth> adminAuths = listAuthInfoByRoleAuthIndex(roleAuthIndices);
        List<Long> authIds = adminAuths.stream().map(auth -> {
            return auth.getId();
        }).collect(Collectors.toList());

        ListAuthPathIdReply reply = ListAuthPathIdReply.newBuilder()
                .addAllAuthIdList(authIds)
                .build();
        return reply;
    }

    @Override
    public ListAllAuthByRoleIdReply listAllAuthByRoleId(ListAllAuthByRoleIdRequest request) {
        List<RoleAuthIndex> roleAuthIndices = listAuthIndexByRoleId(request.getOrgId(), Arrays.asList(request.getRoleId()));
        List<AdminAuth> adminAuths = listAuthInfoByRoleAuthIndex(roleAuthIndices);
        List<AuthInfo> authInfos = adminAuths.stream().map(auth -> {
            AuthInfo.Builder builder = AuthInfo.newBuilder();
            builder.setAuthId(auth.getId());
            builder.setFrontEndPath(auth.getFrontEndPath());
            builder.setPath(auth.getPath());
            Optional<RoleAuthIndex> authIndexOptional = roleAuthIndices.stream().filter(r -> r.getAuthId().equals(auth.getId())).findFirst();
            if (authIndexOptional.isPresent()) {
                builder.setEditAbleStatus(authIndexOptional.get().getEditStatus());
            } else {
                builder.setEditAbleStatus(2); //只读
            }
            return builder.build();
        }).collect(Collectors.toList());

        ListAllAuthByRoleIdReply reply = ListAllAuthByRoleIdReply.newBuilder()
                .addAllAuthPathInfos(authInfos)
                .build();

        return reply;
    }

    @Override
    public ListAllAuthIdReply listAllAuthId(ListAllAuthIdRequest request) {
        List<AdminAuth> adminAuths = listAllAuth();
        List<Long> authIds = adminAuths.stream().map(AdminAuth::getId).collect(Collectors.toList());
        return ListAllAuthIdReply.newBuilder()
                .addAllAuthIds(authIds)
                .build();
    }

    public Boolean isUserHaveRole(Long userId, Long roleId, Long orgId) {
        Example example = new Example(UserRoleIndex.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orgId", orgId);
        criteria.andEqualTo("roleId", roleId);
        criteria.andEqualTo("userId", userId);
        criteria.andEqualTo("status", StatusConstant.PASS_STATUS);
        UserRoleIndex userRoleIndex = userRoleIndexMapper.selectOneByExample(example);
        return Objects.nonNull(userRoleIndex);
    }

    public List<RoleInfo> listRoleInfoByOrgId(Long orgId, Long adminId, Boolean isGetAllRoleInfo) {
        Example configExample = new Example(AdminRole.class);
        Example.Criteria criteria = configExample.createCriteria();
        criteria.andEqualTo("orgId", orgId);
        if (!isGetAllRoleInfo) {
            criteria.andEqualTo("status", StatusConstant.PASS_STATUS);
        }
        List<AdminRole> roleList = roleMapper.selectByExample(configExample);
        List<RoleInfo> roleInfoList = roleList.stream().map(role -> {
            int userCount = userRoleIndexMapper.countByRoleId(StatusConstant.PASS_STATUS, role.getOrgId(), role.getId());
            Boolean enable = false;
            if (adminId != 0L) {
                enable = isUserHaveRole(adminId, role.getId(), orgId);
            }
            RoleInfo roleInfo = RoleInfo.newBuilder()
                    .setRoleId(role.getId())
                    .setName(role.getName())
                    .setOrgId(role.getOrgId())
                    .setUserCount(userCount)
                    .setStatus(role.getStatus())
                    .setEnable(enable)
                    .setCreatedAt(role.getCreatedAt())
                    .build();
            return roleInfo;
        }).collect(Collectors.toList());
        return roleInfoList;
    }

    private List<AdminAuth> listAuthInfoByRoleAuthIndex(List<RoleAuthIndex> roleAuthIndices) {
        if (!CollectionUtils.isEmpty(roleAuthIndices)) {
            List<Long> authIds = roleAuthIndices.stream().map(index -> {
                return index.getAuthId();
            }).collect(Collectors.toList());
            return listAuthInfoByIds(authIds);
        } else {
            return new ArrayList<>();
        }
    }

//    private List<AdminAuth> listAuthInfoByRoleId(Long roleId, Long orgId) {
//        List<RoleAuthIndex> roleAuthIndices = listAuthIndexByRoleId(orgId, Collections.singletonList(roleId));
//        if (!CollectionUtils.isEmpty(roleAuthIndices)) {
//            List<Long> authIds = roleAuthIndices.stream().map(index -> {
//                return index.getAuthId();
//            }).collect(Collectors.toList());
//            return listAuthInfoByIds(authIds);
//        } else {
//            return new ArrayList<>();
//        }
//    }

    private List<RoleAuthIndex> listAuthIndexByUserId(Long userId, Long orgId) {
        List<UserRoleIndex> userRoleIndices = listRoleIndexByUserId(userId, orgId, false);
        if (!CollectionUtils.isEmpty(userRoleIndices)) {
            List<Long> roleIds = userRoleIndices.stream().map(index -> {
                return index.getRoleId();
            }).collect(Collectors.toList());
            return listAuthIndexByRoleId(orgId, roleIds);
        } else {
            return new ArrayList<>();
        }
    }

    private List<AdminAuth> listAuthInfoByIds(List<Long> authIds) {
        Example example = new Example(AdminAuth.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", authIds);
        criteria.andEqualTo("status", AdminAuth.ENABLE_STATUS);
        return authMapper.selectByExample(example);
    }

    private List<AdminAuth> listAllAuth() {
        Example example = new Example(AdminAuth.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", AdminAuth.ENABLE_STATUS);
        return authMapper.selectByExample(example);
    }

    private List<RoleAuthIndex> listAuthIndexByRoleId(Long orgId, List<Long> roleIds) {
        Example example = new Example(RoleAuthIndex.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("roleId", roleIds);
        criteria.andEqualTo("orgId", orgId);
        criteria.andEqualTo("status", RoleAuthIndex.ENABLE_STATUS);

        return roleAuthIndexMapper.selectByExample(example);
    }

    @Override
    public List<AdminRole> listRoleInfoByUserId(Long userId, Long orgId, Boolean isGetAllRoleInfo) {
        List<UserRoleIndex> userRoleIndices = listRoleIndexByUserId(userId, orgId, isGetAllRoleInfo);
        if (!CollectionUtils.isEmpty(userRoleIndices)) {
            List<Long> roleIds = userRoleIndices.stream().map(index -> {
                return index.getRoleId();
            }).collect(Collectors.toList());

            Example example = new Example(AdminRole.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andIn("id", roleIds);
            criteria.andEqualTo("orgId", orgId);
            if (!isGetAllRoleInfo) {
                criteria.andEqualTo("status", AdminRole.ENABLE_STATUS);
            }

            List<AdminRole> adminRoles = roleMapper.selectByExample(example);
            return adminRoles;
        } else {
            return new ArrayList<>();
        }

    }

    @Override
    public List<String> listRoleNameByUserId(Long userId, Long orgId) {
        List<String> result = new ArrayList<>();
        List<AdminRole> adminRoles = listRoleInfoByUserId(userId, orgId, true);
        adminRoles.forEach(role -> {
            result.add(role.getName());
        });
        return result;
    }

    private List<UserRoleIndex> listRoleIndexByUserId(Long userId, Long orgId, Boolean isGetAllRoleInfo) {
        Example example = new Example(UserRoleIndex.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", userId);
        criteria.andEqualTo("orgId", orgId);
        if (!isGetAllRoleInfo) {
            criteria.andEqualTo("status", UserRoleIndex.ENABLE_STATUS);
        }
        return userRoleIndexMapper.selectByExample(example);
    }

    private List<AdminAuth> listAuth(Long orgId) {
        Example example = new Example(AdminAuth.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", 1);
        return authMapper.selectByExample(example);
    }

    private Map<Long, String> mapAuthNameByLocale(String locale) {
        Example example = new Example(AdminAuthDetail.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("locale", locale);
        criteria.andEqualTo("status", StatusConstant.PASS_STATUS);
        List<AdminAuthDetail> adminAuthDetails = authDetailMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(adminAuthDetails)) {
            example.clear();
            Example.Criteria defaultCriteria = example.createCriteria();
            defaultCriteria.andEqualTo("locale", Locale.CHINA.getDisplayLanguage());
            defaultCriteria.andEqualTo("status", StatusConstant.PASS_STATUS);
            adminAuthDetails = authDetailMapper.selectByExample(example);
        }
        Map<Long, String> result = adminAuthDetails.stream().collect(Collectors.toMap(AdminAuthDetail::getAuthId, AdminAuthDetail::getName));
        return result;
    }

    private List<RoleAuthIndex> listRoleAuthIndex(Long orgId, Long roleId) {
        Example example = new Example(RoleAuthIndex.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("roleId", roleId);
        criteria.andEqualTo("orgId", orgId);
        criteria.andEqualTo("status", StatusConstant.PASS_STATUS);
        List<RoleAuthIndex> result = roleAuthIndexMapper.selectByExample(example);
        return result;
    }

    private List<AdminUser> listSubAdminUserByOrgId(Long orgId) {
        Example example = new Example(AdminUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orgId", orgId);
        criteria.andEqualTo("accountType", AdminUser.SUB_ACCOUNT);
        List<AdminUser> adminUserList = userMapper.selectByExample(example);
        adminUserList.forEach(adminUser -> {
            adminUser.setPassword(StringUtils.EMPTY);
        });
        return adminUserList;
    }

    private void updateRoleAuthIndex(Map<Long, Integer> authIdMap, Long roleId, Long orgId) {
        //delete old config by roleId
        deleteAllRoleAuthIndex(roleId, orgId);
        //role -> auth
        insertRoleAuthIndex(authIdMap, roleId, orgId);
    }

    private void insertRoleAuthIndex(Map<Long, Integer> authIdMap, Long roleId, Long orgId) {
        //role -> auth
        authIdMap.forEach((authId, editStatus) -> {
            RoleAuthIndex index = new RoleAuthIndex();
            index.setAuthId(authId);
            index.setRoleId(roleId);
            index.setOrgId(orgId);
            index.setStatus(StatusConstant.PASS_STATUS);
            index.setCreatedAt(System.currentTimeMillis());
            index.setEditStatus(editStatus);
            roleAuthIndexMapper.insert(index);
        });
    }

    private void updateUserRoleIndexByRoleId(List<Long> userIds, Long roleId, Long orgId) {
        //delete old config by roleId
        deleteUserRoleIndexByRoleId(roleId, orgId);
        //role -> user
        insertUserRoleIndexByRoleId(userIds,roleId,orgId);
    }

    private void insertUserRoleIndexByRoleId(List<Long> userIds, Long roleId, Long orgId) {
        //role -> user
        userIds.forEach(userId -> {
            UserRoleIndex index = new UserRoleIndex();
            index.setUserId(userId);
            index.setRoleId(roleId);
            index.setOrgId(orgId);
            index.setStatus(StatusConstant.PASS_STATUS);
            index.setCreatedAt(System.currentTimeMillis());
            userRoleIndexMapper.insert(index);
        });
    }

    private void updateUserRoleIndexByUserId(List<Long> roleIds, Long userId, Long orgId) {
        //delete old config by userId
        deleteUserRoleIndexByUserId(userId, orgId);
        //user -> role
        insertUserRoleIndexByUserId(roleIds, userId, orgId);
    }

    private void insertUserRoleIndexByUserId(List<Long> roleIds, Long userId, Long orgId) {
        //role -> user
        roleIds.forEach(roleId -> {
            UserRoleIndex index = new UserRoleIndex();
            index.setUserId(userId);
            index.setRoleId(roleId);
            index.setOrgId(orgId);
            index.setStatus(StatusConstant.PASS_STATUS);
            index.setCreatedAt(System.currentTimeMillis());
            userRoleIndexMapper.insert(index);
        });
    }

    private void deleteAllRoleAuthIndex(Long roleId, Long orgId) {

        Example exp=new Example(RoleAuthIndex.class);
        exp.createCriteria().andEqualTo("orgId",orgId).andEqualTo("roleId",roleId);
        roleAuthIndexMapper.deleteByExample(exp);
        //roleAuthIndexMapper.deleteConfig(StatusConstant.DELETE_STATUS, StatusConstant.PASS_STATUS, orgId, roleId);
    }

    private void deleteUserRoleIndexByRoleId(Long roleId, Long orgId) {
        Example exp=new Example(UserRoleIndex.class);
        exp.createCriteria().andEqualTo("orgId",orgId).andEqualTo("roleId",roleId);
        userRoleIndexMapper.deleteByExample(exp);
        //userRoleIndexMapper.deleteByRoleId(StatusConstant.DELETE_STATUS, StatusConstant.PASS_STATUS, orgId, roleId);
    }

    private void deleteUserRoleIndexByUserId(Long userId, Long orgId) {

        Example exp=new Example(UserRoleIndex.class);
        exp.createCriteria().andEqualTo("orgId",orgId).andEqualTo("userId",userId);
        userRoleIndexMapper.deleteByExample(exp);
        //userRoleIndexMapper.deleteByUserId(StatusConstant.DELETE_STATUS, StatusConstant.PASS_STATUS, orgId, userId);
    }
}
