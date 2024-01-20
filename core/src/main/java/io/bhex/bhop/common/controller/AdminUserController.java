package io.bhex.bhop.common.controller;


import io.bhex.base.admin.common.*;
import io.bhex.bhop.common.bizlog.BussinessLogAnnotation;
import io.bhex.bhop.common.config.OrgInstanceConfig;
import io.bhex.bhop.common.dto.ChangePasswordDTO;
import io.bhex.bhop.common.dto.param.*;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.bhex.bhop.common.service.AdminSetPasswordService;
import io.bhex.bhop.common.service.AdminUserExtensionService;
import io.bhex.bhop.common.util.RequestUtil;
import io.bhex.bhop.common.util.ResultModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
public class AdminUserController extends BaseController {

    @Resource
    private AdminUserClient adminUserClient;

    @Autowired(required = false)
    private AdminUserExtensionService adminUserExtensionService;

    @Autowired
    private OrgInstanceConfig orgInstanceConfig;
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AdminLoginUserService adminLoginUserService;
    @Autowired
    private AdminSetPasswordService adminSetPasswordService;

    //供saas调用
    @AccessAnnotation(internal = true)
    @BussinessLogAnnotation
    @RequestMapping(value = "/create_user", method = RequestMethod.POST)
    public ResultModel<Long> createUser(HttpServletRequest request,
                                          @RequestBody CreateAdminUserPO userPO) {
        log.info("request info:{}", userPO);
        AdminUserReply userReply = adminUserClient.getAdminRootUserByOrgId(userPO.getOrgId());
        if (userReply != null && userReply.getId() > 0) {
            return ResultModel.validateFail("org.has.existed", userPO.getOrgId());
        }

        AddAdminUserRequest.Builder builder = AddAdminUserRequest.newBuilder()
                .setUsername(userPO.getUsername())
                .setPassword("")
                .setCreatedIp(RequestUtil.getRealIP(request))
                .setAreaCode(StringUtils.isNotEmpty(userPO.getAreaCode()) ? userPO.getAreaCode() : "86")
                .setTelephone(userPO.getTelephone())
                .setOrgId(userPO.getOrgId())
                .setSaasOrgId(userPO.getSaasOrgId())
                .setEmail(userPO.getEmail())
                .setOrgName(userPO.getOrgName())
                .setDefaultLanguage(userPO.getDefaultLanguage());

        AddAdminUserReply reply = adminUserClient.addAdminUser(builder.build());
        log.info("add user result:{}", reply);
        if (null != adminUserExtensionService) {
            adminUserExtensionService.afterCreateUser(userPO);
        }
        orgInstanceConfig.reloadInstancesCache();
        log.info("add user success, type:{}, id:{} brokerId:{}", userPO.getCreateUserType(), reply.getAdminUserId(), userPO.getOrgId());
        return ResultModel.ok(reply.getAdminUserId());
    }

    //供saas调用发送短信使用
    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/send_set_password_email", method = RequestMethod.POST)
    public ResultModel<Boolean> sendSetPasswordEmail(@RequestBody SetPasswordEmailPO po) {
        Long orgId = po.getOrgId();
        AdminUserReply reply = adminUserClient.getAdminRootUserByOrgId(orgId);
        if (reply.getStatus() == 1) {
            return ResultModel.ok(true);
        }
        log.info("brokerId:{} reply:{}", orgId,  reply);

        long adminUserId = reply.getId();
        return adminSetPasswordService.sendSetPasswordEmail(orgId, adminUserId);
    }

    //供saas调用
    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/has_set_password_ok", method = RequestMethod.POST)
    public ResultModel<Boolean> querySetPasswordOk(@RequestBody OrgIdPO po) {
        AdminUserReply reply = adminUserClient.getAdminRootUserByOrgId(po.getOrgId());
        GetInitPasswordTokenReply tokenReply = adminUserClient.getInitPasswordToken(reply.getId());
        if (tokenReply.getValidateResult() == 1) { //validate success
            return ResultModel.ok(true);
        }
        return ResultModel.ok(false);
    }

    //供saas调用 修改管理员信息
    @AccessAnnotation(internal = true)
    @BussinessLogAnnotation
    @RequestMapping(value = "/change_admin_user", method = RequestMethod.POST)
    public ResultModel changeAdminUser(@RequestBody ChangeAdminUserPO userPO) {
        log.info("request info:{}", userPO);
        AdminUserReply userReply = adminUserClient.getAdminUserByEmail(userPO.getEmail(), userPO.getOrgId());
        if (userReply == null || userReply.getId() == 0) {
            return ResultModel.validateFail("email.not.existed", userPO.getEmail());
        }

        if (userReply.getAccountType() == AccountType.ROOT_ACCOUNT) {
            if (userPO.isUnbindGa() || userPO.isUnbindPhone()) {
                return ResultModel.error("can't unbindGA/unbindPhone for root account!");
            }
        }

        if (userPO.isUnlockAdminLogin()) {
            String key = getAdminPlatform().getValue() + ".login.error.times." + userReply.getId();
            log.info("unlock email:{} key:{}", userPO.getEmail(), key);
            redisTemplate.delete(key);
        }

        ChangeAdminUserRequest.Builder builder = ChangeAdminUserRequest.newBuilder();
        BeanUtils.copyProperties(userPO, builder);
//        if (StringUtils.isNotEmpty(userPO.getNewEmail())) {
//            builder.setNewEmail(userPO.getNewEmail());
//            builder.setChangeNewEmail(true);
//        }
        builder.setId(userReply.getId());
        ChangeAdminUserReply reply = adminUserClient.changeAdminUser(builder.build());
        return ResultModel.ok(reply.getMessage());
    }

    @AccessAnnotation(verifyAuth = false, verifyGaOrPhone = false)
    @RequestMapping(value = "/change_password", method = RequestMethod.POST)
    public ResultModel<Void> changePassword(@RequestBody ChangePasswordPO po, HttpServletResponse response) {
        Boolean isOk = adminUserClient.changePassword(getRequestUserId(), po.getOldPassword(), po.getNewPassword());
        if (isOk) {
            adminLoginUserService.loginOut(response, getRequestUserId());
        }
        return ResultModel.ok(ChangePasswordDTO.builder()
                .success(isOk)
                .build());
    }

    @AccessAnnotation(verifyLogin = false)
    @RequestMapping(value = "/init_password", method = RequestMethod.POST)
    public ResultModel<Void> initPassword(@RequestBody @Valid InitPasswordPO initPasswordPO) {
//        if(!initPasswordPO.getPassword().equals(initPasswordPO.getConfirmedPassword())){
//            return ResultModel.validateFail("setpassword.not.equal");
//        }

        GetInitPasswordTokenReply reply = adminUserClient.getInitPasswordToken(initPasswordPO.getAdminUserId());
        log.info("request:{} reply:{}", initPasswordPO, reply);
        if(reply == null || reply.getToken().equals("")){
            return ResultModel.validateFail("request.parameter.error");
        }

        if(reply.getValidateResult() == 1){
            log.info("has been validated");
            return ResultModel.ok();
        }

        if(!reply.getToken().equals(initPasswordPO.getToken())){
            log.info("request token not equal to db. db:{} request:{}", reply.getToken(), initPasswordPO.getToken());
            ResultModel result = ResultModel.validateFail("request.parameter.error");
            result.setData(false);
            return result;
        }

        if(reply.getExpiredAt() < System.currentTimeMillis()){
            ResultModel result = ResultModel.validateFail("setpassword.token.expired");
            result.setData(false);
            return result;
        }

        boolean result = adminUserClient.saveInitPassword(initPasswordPO.getAdminUserId(), initPasswordPO.getPassword());

        return ResultModel.ok(result);
    }

    @AccessAnnotation(verifyLogin = false)
    @RequestMapping(value = "/valid_token", method = RequestMethod.POST)
    public ResultModel validToken(@RequestBody @Valid ValidateTokenPO po) {
        GetInitPasswordTokenReply reply = adminUserClient.getInitPasswordToken(po.getAdminUserId());
        if(reply == null || reply.getToken().equals("")){
            ResultModel result = ResultModel.validateFail("request.parameter.error");
            result.setData(false);
            return result;
        }

        if(reply.getValidateResult() == 1){
            log.info("has been validated");
            return ResultModel.ok();
        }

        if(!reply.getToken().equals(po.getToken())){
            log.info("request token not equal to db. db:{} request:{}", reply.getToken(), po.getToken());
            ResultModel result = ResultModel.validateFail("request.parameter.error");
            result.setData(false);
            return result;
        }

        if(reply.getExpiredAt() < System.currentTimeMillis()){
            ResultModel result = ResultModel.validateFail("setpassword.token.expired");
            result.setData(false);
            return result;
        }

        return ResultModel.ok(true);
    }

}
