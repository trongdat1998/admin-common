package io.bhex.bhop.common.constant;

import io.bhex.bhop.common.enums.AdminPlatformEnum;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.constant
 * @Author: ming.xu
 * @CreateDate: 2019/3/22 11:48 AM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public enum CaptcheType {

    LOGIN_VERIFY_CAPTCHE_MOBILE(10,TimeUnit.MINUTES, "%s|login_verify_mobile|%s_%s", CaptcheDuplicateType.LOGIN_VERIFY_CAPTCHE_MOBILE, CaptcheContent.LOGIN_VERIFY_CAPTCHE_MOBILE),
    BIND_GA_CAPTCHE_EMAIL(10,TimeUnit.MINUTES, "%s|bind_ga_email|%s_%s", CaptcheDuplicateType.BIND_GA_CAPTCHE_EMAIL_DUPLICATE, CaptcheContent.BIND_GA_CAPTCHE_EMAIL),
    BIND_PHONE_CAPTCHE_EMAIL(10,TimeUnit.MINUTES, "%s|bind_phone_email|%s_%s", CaptcheDuplicateType.BIND_PHONE_CAPTCHE_EMAIL_DUPLICATE, CaptcheContent.BIND_PHONE_CAPTCHE_EMAIL),
    BIND_PHONE_CAPTCHE_MOBILE(10,TimeUnit.MINUTES, "%s|bind_phone_mobile|%s_%s", CaptcheDuplicateType.BIND_PHONE_CAPTCHE_MOBILE_DUPLICATE, CaptcheContent.BIND_PHONE_CAPTCHE_MOBILE);

    private Integer time;

    private TimeUnit timeUnit;

    private String name;

    private CaptcheDuplicateType captcheDuplicateType;

    private CaptcheContent captcheContent;

    CaptcheType(Integer time, TimeUnit timeUnit, String name, CaptcheDuplicateType captcheDuplicateType, CaptcheContent captcheContent) {
        this.time = time;
        this.timeUnit = timeUnit;
        this.name = name;
        this.captcheDuplicateType = captcheDuplicateType;
        this.captcheContent = captcheContent;
    }

    public String getName(AdminPlatformEnum platform, Long orgId, String contact) {
        return String.format(name, platform.getValue(), orgId, contact);
    }

    public Integer getExpirSeconds() {
        return (int) this.timeUnit.toSeconds(this.time);
    }

    public CaptcheDuplicateType getCaptcheDuplicateType() {
        return captcheDuplicateType;
    }

    public String getTitleId() {
        return Objects.nonNull(captcheContent)? captcheContent.getTitleId(): null;
    }

    public String getContentId() {
        return Objects.nonNull(captcheContent)? captcheContent.getContentId(): null;
    }

}
