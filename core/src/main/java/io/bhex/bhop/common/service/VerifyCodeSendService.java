package io.bhex.bhop.common.service;

import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.config.LocaleMessageService;
import io.bhex.bhop.common.enums.AdminPlatformEnum;
import io.bhex.bhop.common.enums.VerifyCodeCacheKey;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.grpc.client.MessagePushClient;
import io.bhex.bhop.common.util.Combo2;
import io.bhex.bhop.common.util.LocaleUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Date: 2018/11/14 下午6:28
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@Service
public class VerifyCodeSendService {
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

    @Value("${verify-captcha:true}")
    private Boolean verifyCaptcha;

    @Value("${global-notify-type:1}")
    private Integer globalNotifyType;

    /**
     *
     * @param platform
     * @param orgId
     * @param nationalCode
     * @param phone
     * @param messageKey
     * @param verifyCodeCacheKey
     * @param sign
     * @return
     */
    public Combo2<Boolean, String> sendSmsCaptcha(AdminPlatformEnum platform, Long orgId, String nationalCode, String phone,
                                                  String messageKey, VerifyCodeCacheKey verifyCodeCacheKey, String sign) {
        if (verifyCaptcha && globalNotifyType == 3) {
            return new Combo2<>(false, "Only support email!");
        }
        String receiver = nationalCode + phone;
        return sendCaptcha(platform, orgId, receiver, verifyCodeCacheKey,
                verifyCode -> {
                    messagePushClient.sendVerificationCodeSmsDirectly(orgId, nationalCode, phone, verifyCode, LocaleUtil.getLanguage());
                    //log.info("{} {} {}", receiver, messageKey, verifyCode);
                    return true;
                });
    }

    public Combo2<Boolean, String> sendEmailCaptcha(AdminPlatformEnum platform, Long orgId, String email, String subject,
                                                    String messageKey, VerifyCodeCacheKey verifyCodeCacheKey) {
        if (verifyCaptcha && globalNotifyType == 2) {
            return new Combo2<>(false, "Only support phone!");
        }
        return sendCaptcha(platform, orgId, email, verifyCodeCacheKey,
                verifyCode -> sendEmailVerifyCode(platform, orgId, email, subject, messageKey, verifyCode));
    }

    public Boolean verifySmsCaptcha(AdminPlatformEnum platform, Long orgId, String nationalCode, String phone,
                                      VerifyCodeCacheKey verifyCodeCacheKey, String captcha){
        return verifyCaptcha(platform, orgId, nationalCode+phone,verifyCodeCacheKey, captcha);
    }

    public Boolean verifyEmailCaptcha(AdminPlatformEnum platform, Long orgId, String email,
                                 VerifyCodeCacheKey verifyCodeCacheKey, String captcha){
        return verifyCaptcha(platform, orgId, email,verifyCodeCacheKey, captcha);
    }


    private Boolean verifyCaptcha(AdminPlatformEnum platform, Long orgId, String receiver,
                                  VerifyCodeCacheKey verifyCodeCacheKey, String captcha) {
        if (!verifyCaptcha) {
            deleteCaptchaCache(platform, orgId, receiver, verifyCodeCacheKey);
            return "123456".equals(captcha);
        }
        String captchaKey = getCaptchaKey(platform, orgId, receiver, verifyCodeCacheKey);
//        String code = cacheClient.getString(captchaKey);
        String code = redisTemplate.opsForValue().get(captchaKey);
        if (null == code || !code.equals(captcha)) {

            return false;
        }
        // verify success remove captcha
        deleteCaptchaCache(platform, orgId, receiver, verifyCodeCacheKey);
        return true;
    }

    private Combo2<Boolean, String> sendCaptcha(AdminPlatformEnum platform, Long orgId, String reveiver,
                                                VerifyCodeCacheKey verifyCodeCacheKey, Process process){
        String captchaKey = getCaptchaKey(platform, orgId, reveiver,verifyCodeCacheKey);
        String duplicateKey = "duplicate|" + captchaKey;
//        Object duplicateCache = cacheClient.getObject(duplicateKey);
        Object duplicateCache = redisTemplate.opsForValue().get(duplicateKey);
        if(duplicateCache != null){
            return new Combo2<>(false, "captcha.60s.once");
        }

//        String code = cacheClient.getString(captchaKey);
        String code = redisTemplate.opsForValue().get(captchaKey);
        if (StringUtils.isEmpty(code)) {
            code = RandomStringUtils.randomNumeric(6);
            //log.info(String.format("Send Captcha. [%s] => code: %s", reveiver, code));
        }

        process.sendVerifyCode(code);
//        cacheClient.set(captchaKey, verifyCodeCacheKey.getExpireTimeInSeconds(), code);
//        cacheClient.set(duplicateKey, verifyCodeCacheKey.getRetryLimitInSeconds(), true);
        redisTemplate.opsForValue().set(captchaKey, code, verifyCodeCacheKey.getExpireTimeInSeconds(), TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(duplicateKey, Boolean.TRUE.toString(), verifyCodeCacheKey.getRetryLimitInSeconds(), TimeUnit.SECONDS);


        return new Combo2<>(true, "");
    }

    public interface Process{
        Boolean sendVerifyCode(String verifyCode);
    }

    private Boolean sendEmailVerifyCode(AdminPlatformEnum platform, Long orgId, String email, String subject,
                                     String messageKey, String verifyCode) {
        String senderName = localeMessageService.getMessage("email.sender.name");
        if (platform != AdminPlatformEnum.SAAS_ADMIN_PLATFROM) {
            AdminUserReply adminUserByEmail = adminUserClient.getAdminRootUserByOrgId(orgId);
            senderName = adminUserByEmail.getOrgName();
        }

        String content = localeMessageService.getMessage(messageKey, new Object[]{verifyCode});
        //log.info("{} {} {}", email, messageKey, verifyCode);
        messagePushClient.sendMailDirectly(orgId, email, subject, senderName, content, LocaleUtil.getLanguage());
        return true;
    }

    private String getCaptchaKey(AdminPlatformEnum platform, Long orgId, String receiver, VerifyCodeCacheKey verifyCodeCacheKey){
        String cachekey = platform.getValue() + verifyCodeCacheKey.getName() + verifyCodeCacheKey.getExtraKey() + receiver + orgId;
        log.info("cache key = {}", cachekey);
        return cachekey;
    }

    private void deleteCaptchaCache(AdminPlatformEnum platform, Long orgId, String receiver, VerifyCodeCacheKey verifyCodeCacheKey) {
        String captchaKey = getCaptchaKey(platform, orgId, receiver, verifyCodeCacheKey);
        redisTemplate.delete(captchaKey);
        String duplicateKey = "duplicate|" + captchaKey;
        redisTemplate.delete(duplicateKey);
    }

}
