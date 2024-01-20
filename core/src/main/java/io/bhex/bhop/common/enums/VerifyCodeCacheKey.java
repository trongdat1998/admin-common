package io.bhex.bhop.common.enums;

/**
 * @Description:
 * @Date: 2018/11/14 下午6:30
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public enum  VerifyCodeCacheKey {

    PLATFORM_ACCOUNT_BIND("|platform_account_bind|", 600, 60),
    SET_BAN_SALE_WHITE_LIST("|ban_sale_white_list|", 600, 60),
    RESET_PASSWORD_TOKEN("|reset_pw_token|", 600,  60),
    RESET_PASSWORD_EMAIL_CAPTCHA("|reset_pw_email_captcha|", 600, 60);

    private Integer expireTimeInSeconds;

    private String name;


    private Integer retryLimitInSeconds;

    private String extraKey = "";

    VerifyCodeCacheKey(String name, Integer expireTimeInSeconds, Integer retryLimitInSeconds) {
        this.expireTimeInSeconds = expireTimeInSeconds;

        this.name = name;
        this.retryLimitInSeconds = retryLimitInSeconds;
    }

    public Integer getExpireTimeInSeconds() {
        return expireTimeInSeconds;
    }

    public void setExpireTimeInSeconds(Integer expireTimeInSeconds) {
        this.expireTimeInSeconds = expireTimeInSeconds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRetryLimitInSeconds() {
        return retryLimitInSeconds;
    }

    public void setRetryLimitInSeconds(Integer retryLimitInSeconds) {
        this.retryLimitInSeconds = retryLimitInSeconds;
    }

    public String getExtraKey() {
        return extraKey;
    }

    public void setExtraKey(String extraKey) {
        this.extraKey = extraKey;
    }
}
