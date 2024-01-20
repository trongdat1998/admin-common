package io.bhex.bhop.common.service;

import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.base.redis.client.CacheClient;
import io.bhex.bhop.common.config.LocaleMessageService;
import io.bhex.bhop.common.config.RedisConfig;
import io.bhex.bhop.common.enums.AdminPlatformEnum;
import io.bhex.bhop.common.exception.ErrorCaptchaException;
import io.bhex.bhop.common.exception.OneMinuteDuplicateException;
import io.bhex.bhop.common.exception.UserNotExistException;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.grpc.client.MessagePushClient;
import io.bhex.bhop.common.util.LocaleUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service
 * @Author: ming.xu
 * @CreateDate: 14/10/2018 2:47 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class ResetPasswordService {

//    @Autowired
//    private CacheClient cacheClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AdminUserClient adminUserClient;

    @Autowired
    private MessagePushClient messagePushClient;

    @Autowired
    private LocaleMessageService localeMessageService;

    @Autowired
    private AdminUserSecuriteService adminUserSecuriteService;

    /**
     * 发送重置密码验证码
     * 60s重发限制
     * 10分钟有效
     * 6位数字
     *
     * @param email
     * @return
     */
    public Boolean sendResetPwEmailCaptcha(String email, Long orgId, AdminPlatformEnum platform) throws OneMinuteDuplicateException, UserNotExistException {
        Boolean emailExist = adminUserSecuriteService.isEmailExist(email, orgId, platform);
        if (!emailExist) {
            throw new UserNotExistException();
        }
        String duplicateKey = platform.getValue() + RedisConfig.CACHE_NAME_RESET_PASSWORD_EMAIL_DUPLICATE.getName() + email + orgId;
//        Object duplicateCache = cacheClient.getObject(duplicateKey);
        Object duplicateCache = redisTemplate.opsForValue().get(duplicateKey);

        if (duplicateCache == null) {
            String captchaKey = platform.getValue() + RedisConfig.CACHE_NAME_RESET_PASSWORD_EMAIL_CAPTCHA.getName() + email + orgId;
//            String code = cacheClient.getString(captchaKey);
            String code = redisTemplate.opsForValue().get(captchaKey);
            if (StringUtils.isEmpty(code)) {
                code = RandomStringUtils.randomNumeric(6);
                log.info(String.format("Send Reset Password Email Captcha. [%s] => code: %s", email, code));
            }
            try {
                sendResetPwEmail(email, orgId, code, platform);
//                cacheClient.set(captchaKey, RedisConfig.CACHE_NAME_RESET_PASSWORD_EMAIL_CAPTCHA.getExpirSeconds(), code);
//                cacheClient.set(duplicateKey, RedisConfig.CACHE_NAME_RESET_PASSWORD_EMAIL_DUPLICATE.getExpirSeconds(), true);
                redisTemplate.opsForValue().set(captchaKey, code,
                        RedisConfig.CACHE_NAME_RESET_PASSWORD_EMAIL_CAPTCHA.getExpirSeconds(), TimeUnit.SECONDS);
                redisTemplate.opsForValue().set(duplicateKey, Boolean.TRUE.toString(),
                        RedisConfig.CACHE_NAME_RESET_PASSWORD_EMAIL_DUPLICATE.getExpirSeconds(), TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Send Reset Password Email Captcha. email push error. email = '" + email + "'", e);
            }
        } else {
            throw new OneMinuteDuplicateException();
        }
        return true;
    }

    private Boolean sendResetPwEmail(String email, Long orgId, String code, AdminPlatformEnum platform) {
        String senderName = localeMessageService.getMessage("email.sender.name");
        if (platform != AdminPlatformEnum.SAAS_ADMIN_PLATFROM) {
            AdminUserReply adminUserByEmail = adminUserClient.getAdminUserByEmail(email, orgId);
            senderName = adminUserByEmail.getOrgName();
        }

        messagePushClient.sendMailDirectly(orgId, email, "重置密码邮件", senderName, String.format("您的验证码为%s，验证码有效期10分钟，请确认是您本人操作。", code), LocaleUtil.getLanguage());
        return true;
    }

    /**
     * 校验重置密码的验证码是否有效
     * 如果有效发放'重置密码Token'，用于重置密码
     * @param captcha
     * @param email
     * @return
     */
    public String verifyResetPwCaptcha(String captcha, String email, Long orgId, AdminPlatformEnum platform) throws ErrorCaptchaException {
        String captchaKey = platform.getValue() + RedisConfig.CACHE_NAME_RESET_PASSWORD_EMAIL_CAPTCHA.getName() + email + orgId;
//        String code = cacheClient.getString(captchaKey);
        String code = redisTemplate.opsForValue().get(captchaKey);

        if (null != code && code.equals(captcha)) {
            String resetPwToken = RandomStringUtils.randomAlphanumeric(32);
            String tokenKey = platform.getValue() + RedisConfig.CACHE_NAME_RESET_PASSWORD_TOKEN.getName() + email + orgId;
//            cacheClient.set(tokenKey, RedisConfig.CACHE_NAME_RESET_PASSWORD_TOKEN.getExpirSeconds(), resetPwToken);
            redisTemplate.opsForValue().set(tokenKey, resetPwToken,
                    RedisConfig.CACHE_NAME_RESET_PASSWORD_TOKEN.getExpirSeconds(), TimeUnit.SECONDS);
            // verify success remove captcha
            deleteCaptchaCache(email, orgId, platform);
            return resetPwToken;
        } else {
            throw new ErrorCaptchaException();
        }
    }

    /**
     * 重置密码
     * 需要验证'重置密码Token'是否有效
     * @param resetPwToken
     * @param email
     * @param newPassword
     * @return
     */
    public Boolean resetPassword(String resetPwToken, String email, Long orgId, String newPassword, AdminPlatformEnum platform) throws ErrorCaptchaException, UserNotExistException {
        boolean isOk = false;
//        String originToken = cacheClient.getString(platform.getValue() + RedisConfig.CACHE_NAME_RESET_PASSWORD_TOKEN.getName() + email + orgId);
        String originToken = redisTemplate.opsForValue().get(platform.getValue() + RedisConfig.CACHE_NAME_RESET_PASSWORD_TOKEN.getName() + email + orgId);
        if (null != originToken && originToken.equals(resetPwToken)) {
            //todo: reset  password
            AdminUserReply adminUserByEmail = adminUserClient.getAdminUserByEmail(email, orgId);
            if (null != adminUserByEmail && adminUserByEmail.getId() != 0) {
                isOk = adminUserClient.resetPassword(adminUserByEmail.getId(), newPassword);
                // reset password success remove token
                if (isOk) {
                    deleteResetTokenCache(email, orgId, platform);
                }
            } else {
                throw new UserNotExistException();
            }
        } else {
            throw new ErrorCaptchaException();
        }
        return isOk;
    }

    public void deleteCaptchaCache(String email, Long orgId, AdminPlatformEnum platform) {
        redisTemplate.delete(platform.getValue() + RedisConfig.CACHE_NAME_RESET_PASSWORD_EMAIL_CAPTCHA.getName() + email + orgId);
        redisTemplate.delete(platform.getValue() + RedisConfig.CACHE_NAME_RESET_PASSWORD_EMAIL_DUPLICATE.getName() + email + orgId);
    }

    public void deleteResetTokenCache(String email, Long orgId, AdminPlatformEnum platform) {
        redisTemplate.delete(platform.getValue() + RedisConfig.CACHE_NAME_RESET_PASSWORD_TOKEN.getName() + email + orgId);
    }
}
