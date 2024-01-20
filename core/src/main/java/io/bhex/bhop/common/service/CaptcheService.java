package io.bhex.bhop.common.service;

import io.bhex.bhop.common.constant.CaptcheType;
import io.bhex.bhop.common.enums.AdminPlatformEnum;
import io.bhex.bhop.common.exception.OneMinuteDuplicateException;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service
 * @Author: ming.xu
 * @CreateDate: 2019/3/22 3:40 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
public abstract class CaptcheService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${verify-captcha:true}")
    private Boolean verifyCaptcha;

    public Boolean sendCaptche(ContactInfo contactInfo, Long orgId, AdminPlatformEnum platform, CaptcheType captcheType) throws OneMinuteDuplicateException {
        Boolean duplicateFlag = false;
        Boolean duplicateCheck = Objects.nonNull(captcheType.getCaptcheDuplicateType());
        String duplicateKey = null;
        if (duplicateCheck) {
            duplicateKey = captcheType.getCaptcheDuplicateType().getName(platform, orgId, getContactInfoStr(contactInfo));
            Object duplicateCache = redisTemplate.opsForValue().get(duplicateKey);
            duplicateFlag = Objects.isNull(duplicateCache) ? true : false;
        }
        if (duplicateFlag) {
            String captchaKey = captcheType.getName(platform, orgId, getContactInfoStr(contactInfo));
            String code = redisTemplate.opsForValue().get(captchaKey);
            if (StringUtils.isEmpty(code)) {
                code = RandomStringUtils.randomNumeric(6);
                //log.info(String.format("Send Captcha. [%s] => code: %s", getContactInfoStr(contactInfo), code));
            }
            try {
                sendCaptche(contactInfo, orgId, code, platform, captcheType);
                redisTemplate.opsForValue().set(captchaKey, code,
                        captcheType.getExpirSeconds(), TimeUnit.SECONDS);
                if (duplicateCheck) {
                    redisTemplate.opsForValue().set(duplicateKey, Boolean.TRUE.toString(),
                            captcheType.getCaptcheDuplicateType().getExpirSeconds(), TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                log.error("Send Captcha. contactInfo push error. contactInfo = '" + contactInfo + "'", e);
            }
        } else {
            throw new OneMinuteDuplicateException();
        }
        return true;
    }

    @Data
    @Builder
    public static class ContactInfo {

        private String email;

        private String mobile;

        private String nationalCode;
    }

    abstract void sendCaptche(ContactInfo contactInfo, Long orgId, String code, AdminPlatformEnum platform, CaptcheType captcheType);

    abstract String getContactInfoStr(ContactInfo contactInfo);

    public Boolean verifyCaptche(ContactInfo contactInfo, Long orgId, String code, AdminPlatformEnum platform, CaptcheType captcheType) {
        if (!verifyCaptcha) {
            return "123456".equals(code);
        }
        String captchaKey = captcheType.getName(platform, orgId, getContactInfoStr(contactInfo));
        String captche = redisTemplate.opsForValue().get(captchaKey);
        if (StringUtils.isEmpty(code)) {
            // todo: 验证码为空
            return false;
        }
        try {
            if (Objects.nonNull(captche) && captche.equals(code)) {
                redisTemplate.delete(captchaKey);
                if (Objects.nonNull(captcheType.getCaptcheDuplicateType())) {
                    String duplicateKey = captcheType.getCaptcheDuplicateType().getName(platform, orgId, getContactInfoStr(contactInfo));
                    redisTemplate.delete(duplicateKey);
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("verifyCaptche error. contactInfo = '" + getContactInfoStr(contactInfo) + "'", e);
        }
        return false;
    }
}
