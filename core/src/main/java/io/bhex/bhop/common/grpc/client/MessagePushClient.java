package io.bhex.bhop.common.grpc.client;

import java.util.List;

/**
 * @Description:
 * @Date: 2018/9/20 下午3:58
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public interface MessagePushClient {

    void sendMail(Long orgId, String mailAddress, String businessType, String language, List<String> params);

    void sendMail(Long orgId, Long userId, String mailAddress, String businessType, String language, List<String> params);

    void sendSms(Long orgId, String nationalCode, String mobile, String businessType, String language, List<String> params);

    void sendSms(Long orgId, Long userId, String nationalCode, String mobile, String businessType, String language, List<String> params);
    /**
     * @param orgId
     * @param mailAddress
     * @param subject
     * @param sign 【from中的sendNamer】
     * @param content
     */
    void sendMailDirectly(Long orgId, String mailAddress, String subject, String sign, String content, String language);

//    /**
//     * org发送邮件时，sign【from中的sendNamer】是orgName
//     * @param orgId
//     * @param mailAddress
//     * @param subject
//     * @param content
//     */
//    void sendMailDirectly(Long orgId, String mailAddress, String subject, String content);

//    void sendSms(String nationalCode, String mobile, long templateId, String ...params);

    void sendSmsDirectly(Long orgId, String nationalCode, String mobile, String content, String sign, String language);

    void sendVerificationCodeSmsDirectly(Long orgId, String nationalCode, String mobile, String code, String language);
}
