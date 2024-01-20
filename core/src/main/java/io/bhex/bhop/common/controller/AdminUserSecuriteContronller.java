package io.bhex.bhop.common.controller;

import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.bizlog.BussinessLogAnnotation;
import io.bhex.bhop.common.constant.CaptcheType;
import io.bhex.bhop.common.dto.BeforeCreateGADTO;
import io.bhex.bhop.common.dto.param.*;
import io.bhex.bhop.common.exception.OneMinuteDuplicateException;
import io.bhex.bhop.common.exception.UserNotExistException;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.service.AdminUserSecuriteService;
import io.bhex.bhop.common.util.MaskUtil;
import io.bhex.bhop.common.util.ResultModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.controller
 * @Author: ming.xu
 * @CreateDate: 2019/3/15 6:08 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/security")
public class AdminUserSecuriteContronller extends BaseController {

    @Autowired
    private AdminUserSecuriteService userSecuriteService;

    @Resource
    private AdminUserClient adminUserClient;

    @AccessAnnotation(verifyGaOrPhone = false, verifyAuth = false)
    @RequestMapping("/bind_ga/before")
    public ResultModel beforeBindGA(@RequestBody BeforeCreateGAPO param) {
        //todo: 禁止二次绑定
        AdminUserReply requestUser = getRequestUser();
        BeforeCreateGADTO dto = BeforeCreateGADTO.builder().build();
        if (requestUser.getBindGa()) {
            return ResultModel.ok(dto);
        }
        param.setAdminUserId(requestUser.getId());
        param.setOrgId(requestUser.getOrgId());
        param.setAccountName(requestUser.getUsername());
        param.setOrgName(requestUser.getOrgName());
        dto = userSecuriteService.beforeBindGA(param);
        dto.setEmail(MaskUtil.emailOutSensitive(requestUser.getEmail()));
        return ResultModel.ok(dto);
    }

    @AccessAnnotation(verifyGaOrPhone = false, verifyAuth = false)
    @BussinessLogAnnotation(opContent = "bindGA:{#param.gaCode} ")
    @RequestMapping("/bind_ga")
    public ResultModel bindGA(@RequestBody BindGAPO param) {
        AdminUserReply requestUser = getRequestUser();
        param.setAdminUserId(requestUser.getId());
        param.setOrgId(getOrgId());
        param.setEmail(requestUser.getEmail());
        return ResultModel.ok(userSecuriteService.bindGA(param, getAdminPlatform(), CaptcheType.BIND_GA_CAPTCHE_EMAIL));
    }

    @AccessAnnotation(verifyGaOrPhone = false, verifyAuth = false)
    @BussinessLogAnnotation(opContent = "bindPhone:{#param.phone} ")
    @RequestMapping("/bind_phone")
    public ResultModel bindPhone(@RequestBody BindPhonePO param) {
        AdminUserReply requestUser = getRequestUser();
        param.setAdminUserId(requestUser.getId());
        param.setOrgId(getOrgId());
        param.setEmail(requestUser.getEmail());
        return ResultModel.ok(userSecuriteService.bindPhone(param, getAdminPlatform()));
    }

    @AccessAnnotation(verifyGaOrPhone = false, verifyAuth = false)
    @BussinessLogAnnotation(opContent = "verifyGA")
    @RequestMapping("/verify_ga")
    public ResultModel verifyGA(@RequestBody VerifyGAPO param) {
        AdminUserReply requestUser = getRequestUser();
        param.setAdminUserId(requestUser.getId());
        param.setOrgId(getOrgId());
        return ResultModel.ok(userSecuriteService.verifyGaCode(param));
    }

    @AccessAnnotation(verifyGaOrPhone = false, verifyAuth = false)
    @RequestMapping("/bind_ga/captcha/email")
    public ResultModel sendBindGAEmailCaptche(@RequestBody SendEmailCaptchePO param) {
        Long orgId = getOrgId();
        try {
            param.setEmail(getRequestUser().getEmail());
            userSecuriteService.sendEmailCaptche(param.getEmail(), orgId, getAdminPlatform(), CaptcheType.BIND_GA_CAPTCHE_EMAIL);
            return ResultModel.ok(true);
        } catch (OneMinuteDuplicateException e) {
            return ResultModel.error("user.send.captcha.time.once");
        } catch (UserNotExistException e) {
            return ResultModel.error("user.reset.pw.email.notexist");
        }
    }

    @AccessAnnotation(verifyGaOrPhone = false, verifyAuth = false)
    @RequestMapping("/bind_phone/captcha/email")
    public ResultModel sendBindPhoneEmailCaptche(@RequestBody SendEmailCaptchePO param) {
        Long orgId = getOrgId();
        try {
            param.setEmail(getRequestUser().getEmail());
            userSecuriteService.sendEmailCaptche(param.getEmail(), orgId, getAdminPlatform(), CaptcheType.BIND_PHONE_CAPTCHE_EMAIL);
            return ResultModel.ok(true);
        } catch (OneMinuteDuplicateException e) {
            return ResultModel.error("user.send.captcha.time.once");
        } catch (UserNotExistException e) {
            return ResultModel.error("user.reset.pw.email.notexist");
        }
    }

    @AccessAnnotation(verifyGaOrPhone = false, verifyAuth = false)
    @RequestMapping("/bind_phone/captcha/phone")
    public ResultModel sendPhoneCaptche(@RequestBody SendMobileCaptchePO param) {
        Long orgId = getOrgId();
        try {
            Boolean phoneExist = adminUserClient.isPhoneExist(param.getNationalCode(), param.getPhone(), orgId, getRequestUserId());
            if (phoneExist) {
                return ResultModel.validateFail("phone.has.existed");
            }
            userSecuriteService.sendMobileCaptche(param.getNationalCode(), param.getPhone(), orgId, getAdminPlatform(), CaptcheType.BIND_PHONE_CAPTCHE_MOBILE);
            return ResultModel.ok(true);
        } catch (OneMinuteDuplicateException e) {
            return ResultModel.error("user.send.captcha.time.once");
        } catch (UserNotExistException e) {
            return ResultModel.error("user.reset.pw.phone.notexist");
        }
    }

}
