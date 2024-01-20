package io.bhex.bhop.common.service;

import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.config.LocaleMessageService;
import io.bhex.bhop.common.constant.CaptcheType;
import io.bhex.bhop.common.enums.AdminPlatformEnum;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.grpc.client.MessagePushClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service
 * @Author: ming.xu
 * @CreateDate: 2019/3/22 5:42 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class MobileCaptcheService extends CaptcheService{

    @Autowired
    private MessagePushClient messagePushClient;

    @Autowired
    private LocaleMessageService localeMessageService;

    @Autowired
    private AdminUserClient adminUserClient;

    @Override
    void sendCaptche(ContactInfo contactInfo, Long orgId, String code, AdminPlatformEnum platform, CaptcheType captcheType) {
        // 多语言替换
        Locale locale = LocaleContextHolder.getLocale();
        messagePushClient.sendVerificationCodeSmsDirectly(orgId, contactInfo.getNationalCode(), contactInfo.getMobile(), code, locale.toString());
    }

    @Override
    String getContactInfoStr(ContactInfo contactInfo) {
        return contactInfo.getNationalCode() + contactInfo.getMobile();
    }
}
