package io.bhex.bhop.common.controller;

import com.google.common.collect.Lists;
import io.bhex.base.admin.common.*;
import io.bhex.bhop.common.bizlog.BussinessLogAnnotation;
import io.bhex.bhop.common.dto.AuthPathInfoDTO;
import io.bhex.bhop.common.dto.RoleInfoDTO;
import io.bhex.bhop.common.dto.UserInfoDTO;
import io.bhex.bhop.common.dto.param.*;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.bhex.bhop.common.service.AdminRoleAuthService;
import io.bhex.bhop.common.service.AdminSetPasswordService;
import io.bhex.bhop.common.util.RequestUtil;
import io.bhex.bhop.common.util.ResultModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.controller
 * @Author: ming.xu
 * @CreateDate: 10/12/2018 6:20 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/role_auth")
public class AdminRoleAuthController extends BaseController {

    @Autowired
    private AdminUserClient adminUserClient;

    @Autowired
    private AdminRoleAuthService adminRoleAuthService;
    @Autowired
    private AdminSetPasswordService adminSetPasswordService;

    @Autowired
    private AdminLoginUserService adminLoginUserService;
    /**
     * 新建角色，关联权限。可以同步关联用户
     * @param param
     * @return
     */
    @BussinessLogAnnotation(opContent = "addRole:{#param.name} ")
    @RequestMapping("/role/create")
    public ResultModel addRole(@RequestBody @Valid AddRolePO param) {

        if ((CollectionUtils.isEmpty(param.getAuthPathIds()) && CollectionUtils.isEmpty(param.getAuthPaths()))) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
        }

        param.setOrgId(getOrgId());
        Boolean isOk = adminRoleAuthService.addRole(param);
        return ResultModel.ok(isOk);
    }

    /**
     * 更新角色信息
     * @param param
     * @return
     */
    @BussinessLogAnnotation(opContent = "updateRole:{#param.name} ")
    @RequestMapping("/role/update")
    public ResultModel updateRole(@RequestBody @Valid UpdateRolePO param, AdminUserReply adminUser) {

        if (StringUtils.isBlank(param.getName()) ||
                (CollectionUtils.isEmpty(param.getAuthPathIds()) && CollectionUtils.isEmpty(param.getAuthPaths())) ||
                Objects.isNull(param.getRoleId()) ||
                param.getRoleId().longValue() < 1L) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
        }

        param.setOrgId(adminUser.getOrgId());
        Boolean isOk = adminRoleAuthService.updateRole(param);
        return ResultModel.ok(isOk);
    }

    /**
     * 角色信息列表
     * @param param
     * @return
     */
    @RequestMapping("/role/list")
    public ResultModel listRoleInfo(@RequestBody ListRoleInfoPO param) {
        param.setOrgId(getOrgId());

        param.setAdminId(!Objects.isNull(param.getAdminId()) && param.getAdminId() != 0L ? param.getAdminId() : 0L);
        List<RoleInfoDTO> dto = adminRoleAuthService.listRoleInfo(param);
        return ResultModel.ok(dto);
    }

    /**
     * 用户信息列表
     *      如果参数里有传递角色id，则通过enable字段，判断用户是否拥有此权限
     * @param param
     * @return
     */
    @RequestMapping("/role/user/list")
    public ResultModel listUserInfo(@RequestBody ListUserInfoPO param) {
        param.setOrgId(getOrgId());
        List<UserInfoDTO> dto = adminRoleAuthService.listUserInfo(param);
        return ResultModel.ok(dto);
    }

    /**
     * 获取角色信息
     * @param param
     * @return
     */
    @RequestMapping("/role/show")
    public ResultModel getRoleInfo(@RequestBody GetRoleInfoPO param) {
        param.setOrgId(getOrgId());
        RoleInfoDTO dto = adminRoleAuthService.getRoleInfo(param);
        return ResultModel.ok(dto);
    }

    /**
     * 更新用户的角色列表
     * @return
     */
    @BussinessLogAnnotation
    @RequestMapping("/user/role/update")
    public ResultModel saveUserRole(@RequestBody @Valid SaveUserRolePO param) {

        if (CollectionUtils.isEmpty(param.getRoleIds())) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
        }

        param.setOrgId(getOrgId());
        Boolean isOk = adminRoleAuthService.saveUserRole(param);
        return ResultModel.ok(isOk);
    }

    /**
     * 角色启禁用
     * @param param
     * @return
     */
    @BussinessLogAnnotation
    @RequestMapping("/role/enable")
    public ResultModel enableRole(@RequestBody EnableRolePO param) {
        param.setOrgId(getOrgId());
        Boolean isOk = adminRoleAuthService.enableRole(param);
        return ResultModel.ok(isOk);
    }

    /**
     * 获取全部权限列表
     * @param param
     * @return
     */
    @RequestMapping("/auth_path/list")
    public ResultModel listAuthPathInfo(@RequestBody ListAuthPathInfoPO param) {
        param.setOrgId(getOrgId());
        param.setRoleId(param.getRoleId() == null || param.getRoleId() == 0L ? 0L : param.getRoleId());
        List<AuthPathInfoDTO> dto = adminRoleAuthService.listAuthPathInfo(param);
        return ResultModel.ok(dto);
    }

    /**
     * 获取全部权限id
     * @return
     */
    @RequestMapping("/auth_id/list")
    public ResultModel listAllAuthId() {
        List<Long> dto = adminRoleAuthService.listAllAuthId(getOrgId());
        return ResultModel.ok(dto);
    }

    /**
     * 获取此用户全部的权限id
     * @return
     */
    @AccessAnnotation(verifyGaOrPhone = false, verifyAuth = false)
    @RequestMapping("/user/auth_path/list")
    public ResultModel listAllAuthByUserId(AdminUserReply adminUser) {
        ListAllAuthByUserIdPO param = new ListAllAuthByUserIdPO();
        Long userId = adminUser.getId();
        if (Objects.isNull(userId) || userId.longValue() < 1L) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
        }
        param.setUserId(userId);
        param.setOrgId(adminUser.getOrgId());
        List<Long> authIds = adminRoleAuthService.listAllAuthIdByUserId(param);
        return ResultModel.ok(authIds);
    }

    /**
     * 获取此用户全部的权限id
     * @return
     */
    @AccessAnnotation(verifyGaOrPhone = false, verifyAuth = false)
    @RequestMapping("/user/auth_path/list_v1")
    public ResultModel listAllAuthByUserIdV1(AdminUserReply adminUser) {
        ListAllAuthByUserIdPO param = new ListAllAuthByUserIdPO();
        param.setUserId(adminUser.getId());
        param.setOrgId(adminUser.getOrgId());
        Map<Long, Integer> authIds = adminRoleAuthService.listAllAuthIdByUserId2(param);
        return ResultModel.ok(authIds);
    }

    /**
     * 通过角色id，获取此角色全部的权限id
     * @param param
     * @return
     */
    @RequestMapping("/role/auth_path/list")
    public ResultModel listAllAuthByRoleId(@RequestBody ListAllAuthByRoleIdPO param, AdminUserReply adminUser) {

        if (Objects.isNull(param.getRoleId()) || param.getRoleId().longValue() < 1L) {
            return ResultModel.ok(Lists.newArrayList());
        }

        param.setOrgId(getOrgId());
        List<Long> authIds = adminRoleAuthService.listAllAuthIdByRoleId(param.getOrgId(), param.getRoleId());
        return ResultModel.ok(authIds);
    }

    /**
     * 通过角色id，获取此角色全部的权限id
     * @param param
     * @return
     */
    @RequestMapping("/role/auth_path/list_v1")
    public ResultModel listAllAuthByRoleIdV1(@RequestBody ListAllAuthByRoleIdPO param, AdminUserReply adminUser) {

        if (Objects.isNull(param.getRoleId()) || param.getRoleId().longValue() < 1L) {
            return ResultModel.ok(Lists.newArrayList());
        }

        param.setOrgId(getOrgId());
        Map<Long, Integer> authIds = adminRoleAuthService.listAllAuthIdByRoleIdV1(param.getOrgId(), param.getRoleId());
        return ResultModel.ok(authIds);
    }



    @RequestMapping(value = "/user/create", method = RequestMethod.POST)
    public ResultModel<Long> createSubUser(HttpServletRequest request,
                                           @RequestBody @Valid CreateSubUserPO userPO, AdminUserReply requestUser) {
        log.info("request info:{}", userPO);
        long orgId = requestUser.getOrgId();



//        if (StringUtils.isEmpty(userPO.getPassword())) {
//            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
//        }

        userPO.setOrgId(orgId);
        AdminUserReply userReply = adminUserClient.getAdminUserByEmail(userPO.getEmail(), userPO.getOrgId());
        if (userReply != null && userReply.getId() > 0) {
            return ResultModel.validateFail("email.has.existed", userPO.getOrgId());
        }
        String areaCode = StringUtils.isNotEmpty(userPO.getNationalCode()) ? userPO.getNationalCode() : "86";
        Boolean phoneExist = adminUserClient.isPhoneExist(areaCode, userPO.getTelephone(), userPO.getOrgId(), 0L);
        if (phoneExist) {
            return ResultModel.validateFail("phone.has.existed");
        }

        if (StringUtils.isNotEmpty(userPO.getVerifyCode())) {
            adminLoginUserService.verifyAdvance(userPO.getAuthType(), userPO.getVerifyCode(), requestUser.getId(), orgId, getAdminPlatform());
        } else { //不带验证码只做信息校验
            return ResultModel.ok();
        }


        AddSubAdminUserRequest.Builder builder = AddSubAdminUserRequest.newBuilder()
                .setRealName(userPO.getUsername())
                .setUsername(userPO.getEmail().trim())
     //           .setPassword(userPO.getPassword())
                .setCreatedIp(RequestUtil.getRealIP(request))
                .setAreaCode(areaCode)
                .setOrgName(StringUtils.isNotEmpty(requestUser.getOrgName()) ? requestUser.getOrgName() : StringUtils.EMPTY)
                .setTelephone(userPO.getTelephone())
                .setOrgId(userPO.getOrgId())
                .setEmail(userPO.getEmail().trim())
                .setPosition(userPO.getPosition())
                .addAllRoleIds(CollectionUtils.isEmpty(userPO.getRoleIds()) ? new ArrayList<>() : userPO.getRoleIds());
//                .setDefaultLanguage(userPO.getDefaultLanguage());
        AddAdminUserReply reply = adminUserClient.addSubAdminUser(builder.build());
        log.info("add sub user result:{}", reply);
        log.info("add sub user success, id:{} orgId:{}",  reply.getAdminUserId(), userPO.getOrgId());

        adminSetPasswordService.sendSetPasswordEmail(orgId, reply.getAdminUserId());

        return ResultModel.ok(reply.getAdminUserId());
    }

    @RequestMapping(value = "/send_set_password_email", method = RequestMethod.POST)
    public ResultModel<Boolean> sendSetPasswordEmail(@RequestBody AdminIdPO po) {
        Long orgId = getOrgId();
        AdminUserReply reply = adminUserClient.getAdminUserById(po.getAdminUserId(), orgId);
        if (reply.getStatus() == 1) {
            return ResultModel.ok(true);
        }
        log.info("brokerId:{} reply:{}", orgId,  reply);
        return adminSetPasswordService.sendSetPasswordEmail(orgId, po.getAdminUserId());
    }


    @RequestMapping(value = "/user/show", method = RequestMethod.POST)
    public ResultModel querySubUser(HttpServletRequest request,
                                         @RequestBody ShowSubUserPO userPO) {
        userPO.setOrgId(getOrgId());
        AdminUserReply userReply = adminUserClient.getAdminUserById(userPO.getAdminId(), userPO.getOrgId());
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(userReply, dto);
        dto.setUsername(userReply.getRealName());

        List<String> roseNameList = userReply.getRoleNameListList();
        dto.setRoleNameList(roseNameList);
        dto.setRoleIds(userReply.getRoleIdsList());
        log.info("show sub user result:{}", userReply);
        return ResultModel.ok(dto);
    }

    @BussinessLogAnnotation
    @RequestMapping(value = "/user/update", method = RequestMethod.POST)
    public ResultModel updateSubUser(HttpServletRequest request,
                                           @RequestBody @Valid UpdateSubUserPO userPO, AdminUserReply requestUser) {
        log.info("request info:{}", userPO);
        long orgId = requestUser.getOrgId();
        if(StringUtils.isBlank(userPO.getUsername()) ||
                StringUtils.isEmpty(userPO.getEmail()) ||
                //StringUtils.isEmpty(userPO.getPassword()) ||
                Objects.isNull(userPO.getAdminId()) ||
                userPO.getAdminId().longValue()<1L){
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
        }

        userPO.setOrgId(orgId);
        AdminUserReply userReply = adminUserClient.getAdminUserById(userPO.getAdminId(), orgId);
        if(userReply.getId() != 0 && userReply.getId() != userPO.getAdminId()){
            return ResultModel.validateFail("email.has.existed", userPO.getOrgId());
        }
        String areaCode = StringUtils.isNotEmpty(userPO.getNationalCode()) ? userPO.getNationalCode() : userReply.getAreaCode();
        Boolean phoneExist = adminUserClient.isPhoneExist(areaCode, userPO.getTelephone(), userPO.getOrgId(), userPO.getAdminId());
        if (phoneExist) {
            return ResultModel.validateFail("phone.has.existed");
        }

        if (StringUtils.isNotEmpty(userPO.getVerifyCode())) {
            adminLoginUserService.verifyAdvance(userPO.getAuthType(), userPO.getVerifyCode(), requestUser.getId(), orgId, getAdminPlatform());
        } else { //不带验证码只做信息校验
            return ResultModel.ok();
        }

        UpdateSubAdminUserRequest.Builder builder = UpdateSubAdminUserRequest.newBuilder()
                .setAdminId(userReply.getId())
                .setRealName(userPO.getUsername())

                .setUsername(userReply.getEmail()) //邮箱 密码 不支持后台修改
                .setEmail(userReply.getEmail())
                //.setPassword(StringUtils.isNotEmpty(userPO.getPassword())? userPO.getPassword(): StringUtils.EMPTY)
                .setCreatedIp(RequestUtil.getRealIP(request))
                .setAreaCode(areaCode)
                .setTelephone(userPO.getTelephone())
                .setOrgId(userPO.getOrgId())

                .setPosition(userPO.getPosition())
                .addAllRoleIds(CollectionUtils.isEmpty(userPO.getRoleIds())? new ArrayList<>(): userPO.getRoleIds());
//                .setDefaultLanguage(userPO.getDefaultLanguage());
        UpdateSubAdminUserReply reply = adminUserClient.updateSubAdminUser(builder.build());
        log.info("update sub user result:{}", reply);
        log.info("update sub user success, type:{}, id:{} brokerId:{}", reply.getAdminUserId(), userPO.getOrgId());
        return ResultModel.ok(reply.getAdminUserId());
    }

    @BussinessLogAnnotation
    @RequestMapping(value = "/user/enable", method = RequestMethod.POST)
    public ResultModel<Long> enableSubUser(HttpServletRequest request,
                                           @RequestBody EnableSubUserPO userPO) {
        userPO.setOrgId(getOrgId());
        AdminUserReply userReply = adminUserClient.getAdminUserById(userPO.getAdminId());

        EnableSubAdminUserRequest.Builder builder = EnableSubAdminUserRequest.newBuilder()
                .setAdminId(userPO.getAdminId())
                .setOrgId(userPO.getOrgId())
                .setStatus(userPO.getStatus() == 1 ? 1 : 2);
//                .setDefaultLanguage(userPO.getDefaultLanguage());
        EnableSubAdminUserReply reply = adminUserClient.enableSubAdminUser(builder.build());
        return ResultModel.ok(reply.getAdminUserId());
    }
}
