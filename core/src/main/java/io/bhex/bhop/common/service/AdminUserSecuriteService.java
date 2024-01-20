package io.bhex.bhop.common.service;

import io.bhex.base.admin.*;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.constant.CaptcheType;
import io.bhex.bhop.common.dto.BeforeCreateGADTO;
import io.bhex.bhop.common.dto.BindGADTO;
import io.bhex.bhop.common.dto.VerifyGADTO;
import io.bhex.bhop.common.dto.param.*;
import io.bhex.bhop.common.enums.AdminPlatformEnum;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.exception.OneMinuteDuplicateException;
import io.bhex.bhop.common.exception.UserNotExistException;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.grpc.client.AdminUserSecurityClient;
import io.bhex.broker.common.util.QRCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service
 * @Author: ming.xu
 * @CreateDate: 2019/3/15 6:15 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class AdminUserSecuriteService {

    @Autowired
    private AdminUserSecurityClient adminUserSecurityClient;

    @Autowired
    private AdminUserClient adminUserClient;

    @Autowired
    private EmailCaptcheService emailCaptcheService;

    @Autowired
    private MobileCaptcheService  mobileCaptcheService;

    public BeforeCreateGADTO beforeBindGA(BeforeCreateGAPO param) {
        SecurityBeforeBindGARequest request = SecurityBeforeBindGARequest.newBuilder()
                .setUserId(param.getAdminUserId())
                .setOrgId(param.getOrgId())
                .setAccountName(param.getAccountName())
                .setGaIssuer(param.getOrgName())
                .build();
        SecurityBeforeBindGAResponse response = adminUserSecurityClient.beforeBindGA(request);
        return BeforeCreateGADTO.builder()
                .secretKey(response.getKey())
                .authUrl(response.getOtpAuthTotpUrl())
                .qrcode(QRCodeUtil.generateQRCodeImage(response.getOtpAuthTotpUrl()))
                .build();
    }

    public BindGADTO bindGA(BindGAPO param, AdminPlatformEnum platform, CaptcheType captcheType) {

        //校验邮箱验证码
        Boolean isOk = verifyEmailCaptche(param.getEmail(), param.getOrgId(), param.getVerifyCode(), platform, captcheType);
        if (isOk) {
            SecurityBindGARequest request = SecurityBindGARequest.newBuilder()
                    .setOrgId(param.getOrgId())
                    .setUserId(param.getAdminUserId())
                    .setGaCode(param.getGaCode())
                    .build();
            SecurityBindGAResponse response = adminUserSecurityClient.bindGA(request);
            if (response.getRet().equals(SecurityErrorCode.SUCCESS)) {
                return BindGADTO.builder()
                        .success(true)
                        .build();
            } else {
                log.info("Admin User Bind GA Error: {}, uid => {}", response.getRet().name(), param.getAdminUserId());
                throw new BizException(ErrorCode.VERIFY_GA_ERROR);
            }
        } else {
            throw new BizException(ErrorCode.VERIFY_GA_ERROR);
        }
    }

    public BindGADTO bindPhone(BindPhonePO param, AdminPlatformEnum platform) {

        //校验邮箱验证码
        Boolean verifyEmail = verifyEmailCaptche(param.getEmail(), param.getOrgId(), param.getVerifyCode(), platform, CaptcheType.BIND_PHONE_CAPTCHE_EMAIL);
        Boolean verifyPhone = verifyMobileCaptche(param.getNationalCode(), param.getPhone(), param.getOrgId(), param.getPhoneCaptcha(), platform, CaptcheType.BIND_PHONE_CAPTCHE_MOBILE);

        if (verifyEmail && verifyPhone) {
            SecurityBindPhoneRequest request = SecurityBindPhoneRequest.newBuilder()
                    .setOrgId(param.getOrgId())
                    .setUserId(param.getAdminUserId())
                    .setPhone(param.getPhone())
                    .setNationCode(param.getNationalCode())
                    .build();
            SecurityBindPhoneResponse response = adminUserSecurityClient.bindPhone(request);
            if (response.getRet().equals(SecurityErrorCode.SUCCESS)) {
                return BindGADTO.builder()
                        .success(true)
                        .build();
            } else {
                log.info("Admin User Bind Phone Error: {}, uid => {}", response.getRet().name(), param.getAdminUserId());
                throw new BizException(ErrorCode.UNBIND_FIRST_ERROR);
            }
        } else {
            throw new BizException(ErrorCode.VERIFY_CAPTCHA_ERROR);
        }
    }

    public VerifyGADTO verifyGaCode(VerifyGAPO param) {
        return verifyGaCode(param.getOrgId(), param.getAdminUserId(), param.getGaCode());
    }

    public VerifyGADTO verifyGaCode(Long orgId, Long userId, Integer gaCode) {
        SecurityVerifyGARequest request = SecurityVerifyGARequest.newBuilder()
                .setGaCode(gaCode)
                .setOrgId(orgId)
                .setUserId(userId)
                .build();
        SecurityVerifyGAResponse response = adminUserSecurityClient.verifyGA(request);
        if (response.getRet().equals(SecurityErrorCode.SUCCESS)) {
            return VerifyGADTO.builder()
                    .success(true)
                    .build();
        } else {
            log.info("Admin User Bind GA Error: {}, uid => {}", response.getRet().name(), userId);
            throw new BizException(ErrorCode.VERIFY_GA_ERROR);
        }

    }

    public void sendEmailCaptche(String email, Long orgId, AdminPlatformEnum platform, CaptcheType captcheType) {
        Boolean emailExist = isEmailExist(email, orgId, platform);
        if (!emailExist) {
            throw new UserNotExistException();
        }
        CaptcheService.ContactInfo contactInfo = CaptcheService.ContactInfo.builder()
                .email(email)
                .build();
        emailCaptcheService.sendCaptche(contactInfo, orgId, platform, captcheType);
    }

    public void sendMobileCaptche(String nationalCode, String mobile, Long orgId, AdminPlatformEnum platform, CaptcheType captcheType) {
        CaptcheService.ContactInfo contactInfo = CaptcheService.ContactInfo.builder()
                .mobile(mobile)
                .nationalCode(nationalCode)
                .build();
        mobileCaptcheService.sendCaptche(contactInfo, orgId, platform, captcheType);
    }

    public Boolean verifyEmailCaptche(String email, Long orgId, String code, AdminPlatformEnum platform, CaptcheType captcheType) {
        CaptcheService.ContactInfo contactInfo = CaptcheService.ContactInfo.builder()
                .email(email)
                .build();
        return emailCaptcheService.verifyCaptche(contactInfo, orgId, code, platform, captcheType);
    }

    public Boolean verifyMobileCaptche(String nationalCode, String mobile, Long orgId, String code, AdminPlatformEnum platform, CaptcheType captcheType) {
        CaptcheService.ContactInfo contactInfo = CaptcheService.ContactInfo.builder()
                .mobile(mobile)
                .nationalCode(nationalCode)
                .build();
        return mobileCaptcheService.verifyCaptche(contactInfo, orgId, code, platform, captcheType);
    }

    /**
     * 判断邮箱是否存在
     * @param email
     * @return
     */
    public Boolean isEmailExist(String email, Long orgId, AdminPlatformEnum platformEnum) {
        if (StringUtils.isNotEmpty(email) && null != orgId) {
            if (platformEnum != AdminPlatformEnum.SAAS_ADMIN_PLATFROM && orgId == 0) {
                return false;
            }
            AdminUserReply adminUserByEmail = adminUserClient.getAdminUserByEmail(email, orgId);
            if (null != adminUserByEmail && adminUserByEmail.getId() != 0) {
                return true;
            }
        }
        return false;
    }

    public Boolean changePassword(ChangePasswordPO po) {
        return adminUserClient.changePassword(po.getAdminUserId(), po.getOldPassword(), po.getNewPassword());
    }
}
