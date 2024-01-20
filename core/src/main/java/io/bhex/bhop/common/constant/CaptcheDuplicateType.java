package io.bhex.bhop.common.constant;

import io.bhex.bhop.common.enums.AdminPlatformEnum;

import java.util.concurrent.TimeUnit;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.constant
 * @Author: ming.xu
 * @CreateDate: 2019/3/26 3:03 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public enum CaptcheDuplicateType {

    LOGIN_VERIFY_CAPTCHE_MOBILE(1, TimeUnit.MINUTES, "%s|login_verify_mobile_duplicate|%s_%s"),
    BIND_GA_CAPTCHE_EMAIL_DUPLICATE(1, TimeUnit.MINUTES, "%s|bind_ga_email_duplicate|%s_%s"),
    BIND_PHONE_CAPTCHE_EMAIL_DUPLICATE(1, TimeUnit.MINUTES, "%s|bind_phone_email_duplicate|%s_%s"),
    BIND_PHONE_CAPTCHE_MOBILE_DUPLICATE(1, TimeUnit.MINUTES, "%s|bind_phone_mobile_duplicate|%s_%s");

    private Integer time;

    private TimeUnit timeUnit;

    private String name;

    CaptcheDuplicateType(Integer time, TimeUnit timeUnit, String name) {
        this.time = time;
        this.timeUnit = timeUnit;
        this.name = name;
    }

    public String getName(AdminPlatformEnum platform, Long orgId, String contact) {
        return String.format(name, platform.getValue(), orgId, contact);
    }

    public Integer getExpirSeconds() {
        return (int) this.timeUnit.toSeconds(this.time);
    }
}
