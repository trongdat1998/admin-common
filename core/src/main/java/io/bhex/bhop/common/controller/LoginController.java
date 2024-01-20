package io.bhex.bhop.common.controller;

import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.bizlog.BussinessLogAnnotation;
import io.bhex.bhop.common.constant.CaptcheType;
import io.bhex.bhop.common.dto.LoginInfoDTO;
import io.bhex.bhop.common.dto.LoginUserBaseInfoDTO;
import io.bhex.bhop.common.dto.param.AuthorizeAdvancePO;
import io.bhex.bhop.common.dto.param.ListAllAuthByUserIdPO;
import io.bhex.bhop.common.dto.param.LoginPO;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.exception.OneMinuteDuplicateException;
import io.bhex.bhop.common.exception.UserNotExistException;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.bhex.bhop.common.service.AdminRoleAuthService;
import io.bhex.bhop.common.service.AdminUserSecuriteService;
import io.bhex.bhop.common.util.Combo2;
import io.bhex.bhop.common.util.MaskUtil;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.broker.common.api.client.geetest.SceneEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
public class LoginController extends BaseController {

    @Resource
    private AdminUserClient adminUserClient;

    @Resource
    private AdminLoginUserService adminLoginUserService;

    @Autowired
    private AdminRoleAuthService adminRoleAuthService;

    @Autowired
    private AdminUserSecuriteService adminUserSecuriteService;

    @AccessAnnotation(verifyLogin = false)
    @BussinessLogAnnotation(name = "op.login", opContent = "user:{#loginPO.username} login")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResultModel<LoginInfoDTO> login(HttpServletRequest request, HttpServletResponse response,
                                           @RequestBody @Valid LoginPO loginPO) {

        Long orgId = getOrgId();
        if (orgId == null) {
            return ResultModel.error("error request url");
        }

        // 滑块验证。目前只有邮箱登录所以type为email
        reCaptchaService.validReCaptcha(request, loginPO.getCaptchaId(), loginPO.getChallenge(), loginPO.getCaptchaResponse(), SceneEnum.LOGIN.getName(), loginPO.getUsername());

        Combo2<String, AdminUserReply> combo2 = adminLoginUserService.validateLogin(orgId, loginPO.getUsername(),
                loginPO.getPassword(), getAdminPlatform());
        if (!StringUtils.isEmpty(combo2.getV1())) {
            return ResultModel.validateFail(combo2.getV1());
        }
        AdminUserReply reply = combo2.getV2();
        if (reply == null || reply.getId() == 0) {
            return ResultModel.validateFail("login.info.wrong");
        }
        LoginInfoDTO dto = adminLoginUserService.loginSuccess(reply, response, getAdminPlatform());
        return ResultModel.ok(dto);
    }

    @AccessAnnotation(verifyLogin = false)
    @RequestMapping(value = "/login_user_info", method = RequestMethod.POST)
    public ResultModel<Object> getLoginUserInfo(HttpServletResponse response) {

        Long id = getRequestUserId();
        if (id == null) {
            return ResultModel.error(ErrorCode.LOGIN_TOKEN_ERROR.getCode(), "need.login");
        }
        AdminUserReply reply = adminUserClient.getAdminUserById(id);

        LoginUserBaseInfoDTO dto = LoginUserBaseInfoDTO.builder()
                .username(reply.getUsername())
                .orgId(reply.getOrgId())
                .orgName(reply.getOrgId() != 0 ? adminLoginUserService.getOrgName(reply.getOrgId()) : reply.getOrgName())
                .needBind(!(reply.getBindGa() || reply.getBindPhone()))
                .bindGA(reply.getBindGa())
                .bindPhone(reply.getBindPhone())
                .phone(MaskUtil.mobileOutSensitive(reply.getTelephone()))
                .email(MaskUtil.emailOutSensitive(reply.getEmail()))
                .build();
        // 取消 token 续期
//        String jwtToken = jwtTokenProvider.generateToken(reply.getId()+"");
//        cookieProvider.create(response, Authorize.COOKIE_TOKEN, jwtToken, true);

        return ResultModel.ok(dto);
    }

    @AccessAnnotation(verifyLogin = false)
    @RequestMapping(value = "/authorize_advance", method = RequestMethod.POST)
    public ResultModel mobileAuthorizeAdvance(HttpServletResponse response, @RequestBody AuthorizeAdvancePO param) {
        adminLoginUserService.loginAdvance(response, param, getAdminPlatform());
        return ResultModel.ok();
    }

    /**
     * 获取此用户全部的权限id
     * @return
     */
    @AccessAnnotation(verifyLogin = false)
    @RequestMapping("/auth_path/list")
    public ResultModel listAllAuthByUserId() {
        ListAllAuthByUserIdPO param = new ListAllAuthByUserIdPO();
        param.setUserId(getRequestUserId());
        param.setOrgId(getOrgId());
        List<Long> authIds = adminRoleAuthService.listAllAuthIdByUserId(param);
        return ResultModel.ok(authIds);
    }

    @AccessAnnotation(verifyLogin = false)
    @BussinessLogAnnotation(name = "op.logout")
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ResultModel<Void> logout(HttpServletResponse response) {
        Long userId = getRequestUserId();
        adminLoginUserService.loginOut(response, userId);
        return ResultModel.ok();
    }

    @AccessAnnotation(verifyLogin = false)
    @RequestMapping(value = "/send_sms_verify_code", method = RequestMethod.POST)
    public ResultModel<Void> sendSmsVerifyCode(@RequestBody AuthorizeAdvancePO param) {
        String requestId = param.getRequestId();
        AdminUserReply user = adminLoginUserService.getUserByRequestId(requestId, getAdminPlatform());
        if (user != null && StringUtils.isNotEmpty(user.getTelephone())) {
            try {
                adminUserSecuriteService.sendMobileCaptche(user.getAreaCode(), user.getTelephone(), user.getOrgId(), getAdminPlatform(), CaptcheType.LOGIN_VERIFY_CAPTCHE_MOBILE);
                return ResultModel.ok();
            } catch (OneMinuteDuplicateException e) {
                return ResultModel.error("user.send.captcha.time.once");
            } catch (UserNotExistException e) {
                return ResultModel.error("user.reset.pw.phone.notexist");
            }
        } else {
            return ResultModel.error("user.login.verify.phone.notexist");
        }
    }

    @AccessAnnotation(verifyAuth = false)
    @RequestMapping(value = "/send_a_sms_verify_code", method = RequestMethod.POST)
    public ResultModel<Void> sendAdminSmsVerifyCode() {
        AdminUserReply user = getRequestUser();
        if (Objects.nonNull(user) && StringUtils.isNotEmpty(user.getTelephone())) {
            try {
                adminUserSecuriteService.sendMobileCaptche(user.getAreaCode(), user.getTelephone(), user.getOrgId(), getAdminPlatform(), CaptcheType.LOGIN_VERIFY_CAPTCHE_MOBILE);
                return ResultModel.ok();
            } catch (OneMinuteDuplicateException e) {
                return ResultModel.error("user.send.captcha.time.once");
            } catch (UserNotExistException e) {
                return ResultModel.error("user.reset.pw.phone.notexist");
            }
        } else {
            return ResultModel.error("user.login.verify.phone.notexist");
        }
    }

    @AccessAnnotation(verifyAuth = false)
    @RequestMapping(value = "/send_saas_sms_verify_code", method = RequestMethod.POST)
    public ResultModel<Void> sendSaasAdminSmsVerifyCode(AdminUserReply user) {
        if (Objects.nonNull(user) && StringUtils.isNotEmpty(user.getTelephone())) {
            try {
                adminUserSecuriteService.sendMobileCaptche(user.getAreaCode(), user.getTelephone(), user.getOrgId(), getAdminPlatform(), CaptcheType.LOGIN_VERIFY_CAPTCHE_MOBILE);
                return ResultModel.ok();
            } catch (OneMinuteDuplicateException e) {
                return ResultModel.error("user.send.captcha.time.once");
            } catch (UserNotExistException e) {
                return ResultModel.error("user.reset.pw.phone.notexist");
            }
        } else {
            return ResultModel.error("user.login.verify.phone.notexist");
        }
    }
}
