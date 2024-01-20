package io.bhex.bhop.common.service;


import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.config.LocaleMessageService;
import io.bhex.bhop.common.config.OrgInstanceConfig;
import io.bhex.bhop.common.enums.AdminPlatformEnum;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.grpc.client.MessagePushClient;
import io.bhex.bhop.common.util.LocaleUtil;
import io.bhex.bhop.common.util.ResultModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class AdminSetPasswordService {

    @Resource
    private AdminUserClient adminUserClient;
    @Autowired
    private MessagePushClient messagePushClient;
    @Autowired
    private LocaleMessageService localeMessageService;
    @Autowired
    private OrgInstanceConfig orgInstanceConfig;
    @Resource
    private BaseCommonService baseCommonService;

    public ResultModel<Boolean> sendSetPasswordEmail(long orgId, long adminUserId) {

        AdminUserReply reply = adminUserClient.getAdminUserById(adminUserId, orgId);
        if (reply.getStatus() == 1) {
            return ResultModel.ok(true);
        }
        String token = adminUserClient.saveInitPasswordToken(adminUserId);

        if (token == null) {
            return ResultModel.validateFail("internal.error");
        }

        String senderName = reply.getOrgName();
        String url = getAdminWebUrl(orgId) + "password/init/" + adminUserId + "/" + token;
        String content = localeMessageService.getMessage("email.setpassword.content", new Object[]{url});
        String subject = localeMessageService.getMessage("email.subject");
        log.info("mail content:{}", url);
        messagePushClient.sendMailDirectly(orgId, reply.getEmail(), subject, senderName, content, LocaleUtil.getLanguage());
        return ResultModel.ok(true);
    }

    private String getAdminWebUrl(Long orgId) {
        AdminPlatformEnum adminPlatform = baseCommonService.getAdminPlatform();
        switch (adminPlatform) {
            case SAAS_ADMIN_PLATFROM: return baseCommonService.getSaasAdminUrl();
            case BROKER_ADMIN_PLATFROM: return orgInstanceConfig.getAdminWebUrlByBrokerId(orgId);
            case EXCHANGE_ADMIN_PLATFROM: return orgInstanceConfig.getAdminWebUrlByExchangeId(orgId);
        }
        return null;
    }
}
