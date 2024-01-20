package io.bhex.bhop.common.controller;

import io.bhex.bhop.common.dto.param.ResetPasswordPO;
import io.bhex.bhop.common.dto.param.SendEmailCaptchePO;
import io.bhex.bhop.common.dto.param.VerifyEmailCaptchaPO;
import io.bhex.bhop.common.exception.ErrorCaptchaException;
import io.bhex.bhop.common.exception.OneMinuteDuplicateException;
import io.bhex.bhop.common.exception.UserNotExistException;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.service.ResetPasswordService;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.broker.common.api.client.geetest.SceneEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.controller
 * @Author: ming.xu
 * @CreateDate: 14/10/2018 11:52 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@RestController
@RequestMapping("/api/v1/user/reset_password")
public class ResetPasswordController extends BaseController {

    @Autowired
    private ResetPasswordService resetPasswordService;

    @AccessAnnotation(verifyLogin = false)
    @RequestMapping(method = RequestMethod.POST)
    public ResultModel resetPassword(@RequestBody ResetPasswordPO param) {
        Long orgId = getOrgId();
        try {
            Boolean isOk = resetPasswordService.resetPassword(param.getResetPwToken(), param.getEmail(), orgId, param.getPassword(), getAdminPlatform());
            if (isOk) {
                return ResultModel.ok();
            } else {
                return ResultModel.error("");
            }
        } catch (ErrorCaptchaException e) {
            return ResultModel.error("user.reset.pw.token.error");
        } catch (UserNotExistException e) {
            return ResultModel.error("user.reset.pw.email.notexist");
        }
    }

    @AccessAnnotation(verifyLogin = false)
    @RequestMapping(value = "/email_captcha", method = RequestMethod.POST)
    public ResultModel sendEmailCaptcha(HttpServletRequest request, @RequestBody SendEmailCaptchePO param) {

        // 滑块验证。目前只有邮箱登录所以type为email
        reCaptchaService.validReCaptcha(request, param.getCaptchaId(), param.getChallenge(), param.getCaptchaResponse(), SceneEnum.EMAIL.getName(), param.getEmail());

        Long orgId = getOrgId();
        try {
            Boolean isOk = resetPasswordService.sendResetPwEmailCaptcha(param.getEmail(), orgId, getAdminPlatform());
            if (isOk) {
                return ResultModel.ok();
            } else {
                return ResultModel.error("user.reset.pw.email.send.error");
            }
        } catch (OneMinuteDuplicateException e) {
            return ResultModel.error("user.reset.pw.captcha.60s.once");
        } catch (UserNotExistException e) {
            return ResultModel.error("user.reset.pw.email.notexist");
        }
    }

    @AccessAnnotation(verifyLogin = false)
    @RequestMapping(value = "/verify_email_captcha", method = RequestMethod.POST)
    public ResultModel verifyEmailCaptcha(@RequestBody VerifyEmailCaptchaPO param) {
        Long orgId = getOrgId();
        try {
            String resetPwToken = resetPasswordService.verifyResetPwCaptcha(param.getCaptcha(), param.getEmail(), orgId, getAdminPlatform());
            return ResultModel.ok(resetPwToken);
        } catch (ErrorCaptchaException e) {
            return ResultModel.error("user.reset.pw.captcha.error");
        }
    }
}
