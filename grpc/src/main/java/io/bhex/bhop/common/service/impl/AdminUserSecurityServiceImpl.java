package io.bhex.bhop.common.service.impl;

import com.google.common.base.Strings;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import io.bhex.base.admin.*;
import io.bhex.bhop.common.entity.AdminUser;
import io.bhex.bhop.common.entity.UserBindGACheck;
import io.bhex.bhop.common.mapper.AdminUserMapper;
import io.bhex.bhop.common.mapper.UserBindGACheckMapper;
import io.bhex.bhop.common.service.AdminUserSecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service.impl
 * @Author: ming.xu
 * @CreateDate: 2019/3/14 2:46 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class AdminUserSecurityServiceImpl implements AdminUserSecurityService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private UserBindGACheckMapper userBindGACheckMapper;

    @Value("${security.skipSendVerifyCode:false}")
    private Boolean skipSendVerifyCode;

    @Override
    public SecurityBeforeBindGAResponse beforeBindGa(Long orgId, Long userId, String gaIssuer, String accountName) {
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();

        UserBindGACheck bindGACheck = UserBindGACheck.builder()
                .orgId(orgId)
                .userId(userId)
                .gaKey(key.getKey())
                .expired(System.currentTimeMillis() + 2 * 3600 * 1000)
                .created(System.currentTimeMillis())
                .build();
        userBindGACheckMapper.insertUserBindGACheck(bindGACheck);

        String otpAuthTotpURL = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(gaIssuer, accountName, key);
        return SecurityBeforeBindGAResponse.newBuilder()
                .setOtpAuthTotpUrl(otpAuthTotpURL)
                .setKey(key.getKey())
                .build();
    }

    @Override
    public SecurityBindGAResponse bindGA(Long orgId, Long userId, Integer gaCode) {
        UserBindGACheck bindGACheck = userBindGACheckMapper.getLastBindGaCheck(orgId, userId);
        if (bindGACheck == null) {
            log.warn("cannot find user bind_ga check record, maybe this request is invalid, userId:{}", userId);
            return SecurityBindGAResponse.newBuilder().setRet(SecurityErrorCode.REQUEST_INVALID).build();
        }
        if (bindGACheck.getExpired() < System.currentTimeMillis()) {
            log.error("user bind_ga check record is expired, userId:{}", userId);
        }
        String gaKey = bindGACheck.getGaKey();
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        if (!googleAuthenticator.authorize(gaKey, gaCode, System.currentTimeMillis())) {
            return SecurityBindGAResponse.newBuilder().setRet(SecurityErrorCode.GA_VALID_ERROR).build();
        }
        AdminUser user = adminUserMapper.selectAdminUserByIdAndOrgId(userId, orgId);
        if (Strings.isNullOrEmpty(user.getGaKey())) {
            user.setGaKey(gaKey);
            user.setBindGa(AdminUser.BIND);
            adminUserMapper.updateByPrimaryKey(user);
            return SecurityBindGAResponse.newBuilder().build();
        } else {
            return SecurityBindGAResponse.newBuilder().setRet(SecurityErrorCode.GA_UNBIND_FIRST).build();
        }
    }

    @Override
    public SecurityVerifyGAResponse verifyGA(Long orgId, Long userId, Integer gaCode) {
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        AdminUser user = adminUserMapper.selectAdminUserByIdAndOrgId(userId, orgId);
        if (!googleAuthenticator.authorize(Strings.nullToEmpty(user.getGaKey()), gaCode, System.currentTimeMillis())) {
            return SecurityVerifyGAResponse.newBuilder().setRet(SecurityErrorCode.GA_VALID_ERROR).build();
        }
        return SecurityVerifyGAResponse.newBuilder().build();
    }

    @Override
    public SecurityBindPhoneResponse bindPhone(Long orgId, Long userId, String nationalCode, String phone) {
        AdminUser user = adminUserMapper.selectAdminUserByIdAndOrgId(userId, orgId);
        if (AdminUser.UN_BIND.equals(user.getBindPhone())) {
            user.setAreaCode(nationalCode);
            user.setTelephone(phone);
            user.setBindPhone(AdminUser.BIND);
            adminUserMapper.updateByPrimaryKey(user);
            return SecurityBindPhoneResponse.newBuilder().build();
        } else {
            return SecurityBindPhoneResponse.newBuilder().setRet(SecurityErrorCode.GA_UNBIND_FIRST).build();
        }
    }

}
