package io.bhex.bhop.common.constant;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.constant
 * @Author: ming.xu
 * @CreateDate: 2019/3/22 11:49 AM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public enum CaptcheContent {

    LOGIN_VERIFY_CAPTCHE_MOBILE("login.verify.captche.mobile.title", "login.verify.captche.mobile.content"),
    BIND_GA_CAPTCHE_EMAIL("bind-ga.captche.email.title", "bind-ga.captche.email.content"),
    BIND_PHONE_CAPTCHE_EMAIL("bind-phone.captche.email.title", "bind-phone.captche.email.content"),
    BIND_PHONE_CAPTCHE_MOBILE("bind-phone.captche.mobile.title", "bind-phone.captche.mobile.content");

    private String titleId;

    private String contentId;

    CaptcheContent(String titleId, String contentId) {
        this.titleId = titleId;
        this.contentId = contentId;
    }

    public String getTitleId() {
        return titleId;
    }

    public String getContentId() {
        return contentId;
    }

}
