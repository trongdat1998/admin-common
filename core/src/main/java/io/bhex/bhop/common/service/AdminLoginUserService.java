package io.bhex.bhop.common.service;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.base.admin.common.LoginByUsernameAndPasswordReply;
import io.bhex.bhop.common.config.LocaleMessageService;
import io.bhex.bhop.common.config.OrgInstanceConfig;
import io.bhex.bhop.common.config.RedisConfig;
import io.bhex.bhop.common.constant.CaptcheType;
import io.bhex.bhop.common.dto.LoginInfoDTO;
import io.bhex.bhop.common.dto.VerifyGADTO;
import io.bhex.bhop.common.dto.param.AuthorizeAdvancePO;
import io.bhex.bhop.common.dto.param.BrokerInstanceRes;
import io.bhex.bhop.common.dto.param.ExchangeInstanceRes;
import io.bhex.bhop.common.enums.AdminPlatformEnum;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.grpc.client.MessagePushClient;
import io.bhex.bhop.common.jwt.authorize.Authorize;
import io.bhex.bhop.common.jwt.authorize.CookieProvider;
import io.bhex.bhop.common.jwt.authorize.JwtTokenProvider;
import io.bhex.bhop.common.util.Combo2;
import io.bhex.bhop.common.util.LocaleUtil;
import io.bhex.broker.common.util.CryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Date: 2018/9/27 下午3:35
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Slf4j
@Service
public class AdminLoginUserService {

    private final static Integer GA_AUTH_TYPE = 1;
    private final static Integer PHONE_AUTH_TYPE = 2;

    private final static String SAAS_ADMIN_LOGIN_OTHER_PLACE = "SAAS_ADMIN_LOGIN_OTHER_PLACE";
    private final static String BROKER_ADMIN_LOGIN_OTHER_PLACE = "BROKER_ADMIN_LOGIN_OTHER_PLACE";
    private final static String EX_ADMIN_LOGIN_OTHER_PLACE = "EX_ADMIN_LOGIN_OTHER_PLACE";

    private final static String ADMIN_LOGIN_EMAIL_CONTENT = "admin.login.email.content";
    private final static String ADMIN_LOGIN_SMS_CONTENT = "admin.login.sms.content";

//    @Autowired
//    @Qualifier(value="redisClient")
//    private CacheClient redisClient;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @Autowired
    private LocaleMessageService localeMessageService;
    @Resource
    private AdminUserClient adminUserClient;

    @Resource
    private JwtTokenProvider jwtTokenProvider;
    @Resource
    private CookieProvider cookieProvider;
    @Resource
    private AdminUserSecuriteService adminUserSecuriteService;

    @Autowired
    private Environment environment;

    @Autowired
    private MessagePushClient messagePushClient;

    @Resource
    private BaseCommonService baseCommonService;

    @Resource
    private OrgInstanceConfig orgInstanceConfig;

    private Boolean gaEnable;

    @PostConstruct
    public void init() {
        gaEnable = Boolean.valueOf(environment.getProperty(Authorize.GA_ENABLE));
    }

    public Combo2<String, AdminUserReply> validateLogin(Long orgId, String username, String password, AdminPlatformEnum platform) {
        LoginByUsernameAndPasswordReply reply = adminUserClient.loginByUsernameAndPassword(orgId, username, password);
        LoginByUsernameAndPasswordReply.Result result = reply.getResult();

        if (result.getNumber() == LoginByUsernameAndPasswordReply.Result.UsernameNotExisted_VALUE) {
            log.info("username:{} not existed", username);
            return new Combo2<>(localeMessageService.getMessage("login.info.wrong"), null);
        }

        AdminUserReply adminUserReply = reply.getAdminUser();
        if (adminUserReply.getStatus() == 2) {
            log.info("{} account be locked", username);
            return new Combo2<>(localeMessageService.getMessage("account.be.locked"), null);
        }

        String key = platform.getValue() + ".login.error.times." + adminUserReply.getId();
        String wrongTimesObj = redisTemplate.opsForValue().get(key);
        Long wrongTimes = wrongTimesObj == null ? null : Long.parseLong(wrongTimesObj);
        if (wrongTimes != null && wrongTimes >= 5) {
            log.info("{} login error time greater than 5 times", username);
            return new Combo2<>(localeMessageService.getMessage("login.info.wrong.times.gt5"), null);
        }

        if (result.getNumber() == LoginByUsernameAndPasswordReply.Result.LoginSuccess_VALUE) {
            if (wrongTimes != null) {
                redisTemplate.delete(key);
            }
            return new Combo2<>("", adminUserReply);
        }


        wrongTimes = wrongTimes == null ? 1L : wrongTimes + 1;
//        redisClient.set(key, 3600*24, wrongTimes);
        redisTemplate.opsForValue().set(key, wrongTimes.toString(), 3600*24, TimeUnit.SECONDS);
        log.info("{} login error time {} times", username, wrongTimes);
//        if(wrongTimes == 1){
//            redisClient.expire(key, 3600*24);
//        }￿

        return new Combo2<>(localeMessageService.getMessage("login.info.wrong.times." + wrongTimes),null);

    }

    public LoginInfoDTO loginSuccess(AdminUserReply reply, HttpServletResponse response, AdminPlatformEnum adminPlatform) {

        String requestId = CryptoUtil.getRandomCode(32);
        String jwtToken = jwtTokenProvider.generateToken(reply.getOrgId(), String.valueOf(reply.getId()));
        Boolean bindGa = reply.getBindGa();
        Boolean bindPhone = reply.getBindPhone();
        if (!gaEnable || !(bindGa || bindPhone)) {
            setCookie(response, jwtToken, reply);
        } else {
            redisTemplate.opsForValue().set(adminPlatform.getValue() + RedisConfig.CACHE_NAME_LOGIN_CURRENT_AU_TOKEN.getName() + requestId, jwtToken);
        }
        if (!gaEnable) {
            bindGa = false;
            bindPhone = false;
        }
        LoginInfoDTO dto = LoginInfoDTO.builder()
                .orgId(reply.getOrgId())
                .username(reply.getUsername())
                .orgName(reply.getOrgId() != 0 ? getOrgName(reply.getOrgId()) : reply.getOrgName())
                .bindGA(bindGa)
                .bindPhone(bindPhone)
                .requestId(requestId)
                .build();
        return dto;
    }

    public String getOrgName(long orgId) {
        if (orgId >= 6000) {
            BrokerInstanceRes instanceRes = orgInstanceConfig.getBrokerInstance(orgId);
            return instanceRes != null ? instanceRes.getBrokerName() : "";
        } else if (orgId != 0) {
            ExchangeInstanceRes instanceRes = orgInstanceConfig.getExchangeInstance(orgId);
            return instanceRes != null ? instanceRes.getExchangeName() : "";
        }
        return "";
    }

    public void loginOut(HttpServletResponse response, Long userId) {
        Optional.ofNullable(userId)
                .orElseThrow(() -> new BizException(ErrorCode.LOGIN_TOKEN_ERROR));
        cookieProvider.clear(response, Authorize.COOKIE_TOKEN);
        cookieProvider.clear(response, Authorize.LOGIN_COOKIE);
        cookieProvider.clear(response, Authorize.ATTRIBUTE_USER_ID);
        logout(userId);
    }


    public void logout(Long userid) {
        try {
            String key = String.format(JwtTokenProvider.USER_TOKEN_KEY, userid);
            log.info("key:{} userid:{} r:{}", key, userid, userid.equals(341236601520914688L));
            if (!userid.equals(341236601520914688L)) {
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.error("logout remove redis token error. userId => {}.", userid, e);
        }
    }

    public void loginAdvance(HttpServletResponse response, AuthorizeAdvancePO param, AdminPlatformEnum adminPlatform) {
        String requestId = param.getRequestId();
        if (StringUtils.isNotEmpty(requestId)) {
            String jwtToken = redisTemplate.opsForValue().get(adminPlatform.getValue() + RedisConfig.CACHE_NAME_LOGIN_CURRENT_AU_TOKEN.getName() + requestId);
            if (StringUtils.isNotEmpty(jwtToken)) {
                String subject = null;
                try {
                    subject = jwtTokenProvider.parseSubject(jwtToken);
                } catch (BizException e) {
                    throw e;
                }
                if (StringUtils.isNotEmpty(subject)) {
                    Long userId = Long.valueOf(subject);
                    AdminUserReply user = adminUserClient.getAdminUserById(userId);
                    // verify captche
                    Boolean verifySuccess = false;
                    if (GA_AUTH_TYPE.equals(param.getAuthType())) {
                        try {
                            VerifyGADTO verifyGADTO = adminUserSecuriteService.verifyGaCode(param.getOrgId(), userId, Integer.parseInt(param.getVerifyCode()));
                            verifySuccess = verifyGADTO.getSuccess();
                        } catch (NumberFormatException e) {
                            throw new BizException(ErrorCode.VERIFY_GA_ERROR);
                        }
                    } else if (PHONE_AUTH_TYPE.equals(param.getAuthType())) {
                        verifySuccess = adminUserSecuriteService.verifyMobileCaptche(user.getAreaCode(), user.getTelephone(), user.getOrgId(), param.getVerifyCode(), adminPlatform, CaptcheType.LOGIN_VERIFY_CAPTCHE_MOBILE);
                    }
                    // verify success & login
                    if (verifySuccess) {
                        redisTemplate.delete(adminPlatform.getValue() + RedisConfig.CACHE_NAME_LOGIN_CURRENT_AU_TOKEN.getName() + requestId);
                        setCookie(response, jwtToken, user);
                        return;
                    } else {
                        throw new BizException(ErrorCode.VERIFY_GA_ERROR);
                    }
                }
            }
        }
        throw new BizException(ErrorCode.LOGIN_TOKEN_ERROR);
    }

    public void verifyAdvance(Integer authType, String verifyCode, Long userId, Long orgId, AdminPlatformEnum adminPlatform) {
        if (Objects.isNull(authType) || StringUtils.isEmpty(verifyCode)) {
            log.info("verifyAdvance error: verifyCode or authType is null. userId =>{}.", userId);
            throw new BizException(ErrorCode.VERIFY_GA_ERROR);
        }

        String userCodeKey = "verifyAdvance." + orgId + "-" + userId + "-" + verifyCode;
        boolean suc = redisTemplate.opsForValue().setIfAbsent(userCodeKey, "", 1, TimeUnit.MINUTES);
        if (!suc) {
            log.info("verifyAdvance error: repeated op. userId =>{}.", userId);
            throw new BizException(ErrorCode.VERIFY_GA_ERROR);
        }

        AdminUserReply user = adminUserClient.getAdminUserById(userId);
        if (Objects.isNull(user)) {
            log.info("verifyAdvance error: user not exist. userId =>{}.", userId);
            throw new BizException(ErrorCode.VERIFY_GA_ERROR);
        }
        // verify captche
        Boolean verifySuccess = false;
        if (GA_AUTH_TYPE.equals(authType)) {
            try {
                VerifyGADTO verifyGADTO = adminUserSecuriteService.verifyGaCode(orgId, userId, Integer.parseInt(verifyCode));
                verifySuccess = verifyGADTO.getSuccess();
                if (!verifySuccess) {
                    log.info("verifyAdvance error: GA verify failed. verifyCode =>{}.", verifyCode);
                    throw new BizException(ErrorCode.VERIFY_GA_ERROR);
                }
            } catch (NumberFormatException e) {
                log.info("verifyAdvance error: NumberFormatException. verifyCode =>{}.", verifyCode);
                throw new BizException(ErrorCode.VERIFY_GA_ERROR);
            }
        } else if (PHONE_AUTH_TYPE.equals(authType)) {
            verifySuccess = adminUserSecuriteService.verifyMobileCaptche(user.getAreaCode(), user.getTelephone(), user.getOrgId(), verifyCode, adminPlatform, CaptcheType.LOGIN_VERIFY_CAPTCHE_MOBILE);
            log.info("verifyAdvance error: phone captche verify failed. verifyCode =>{}.", verifyCode);
            if (!verifySuccess) {
                throw new BizException("verify.phone.capture.error");
            }
        }
    }



    public AdminUserReply getUserByRequestId(String requestId, AdminPlatformEnum adminPlatform) {
        if (StringUtils.isNotEmpty(requestId)) {
            String jwtToken = redisTemplate.opsForValue().get(adminPlatform.getValue() + RedisConfig.CACHE_NAME_LOGIN_CURRENT_AU_TOKEN.getName() + requestId);
            if (StringUtils.isNotEmpty(jwtToken)) {
                String subject = null;
                try {
                    subject = jwtTokenProvider.parseSubject(jwtToken);
                } catch (BizException e) {
                    throw e;
                }
                if (StringUtils.isNotEmpty(subject)) {
                    Long userId = Long.valueOf(subject);
                    AdminUserReply user = adminUserClient.getAdminUserById(userId);
                    return user;
                }
            }
        }
        return null;
    }

    private void setCookie(HttpServletResponse response, String jwtToken, AdminUserReply user) {
        cookieProvider.create(response, Authorize.COOKIE_TOKEN, jwtToken, true);
        cookieProvider.create(response, Authorize.LOGIN_COOKIE, user.getUsername(), false);
        cookieProvider.create(response, Authorize.ATTRIBUTE_USER_ID, user.getId() + "", false);
    }

    public void repeatedLoginAlarm(Long orgId, Long userId) {
        AdminPlatformEnum adminPlatform = baseCommonService.getAdminPlatform();
        AdminUserReply reply = adminUserClient.getAdminUserById(userId);
        if (Objects.nonNull(reply)) {
            if (StringUtils.isNotEmpty(reply.getEmail())) {
                List<String> paramList = new ArrayList();
                paramList.add(Strings.nullToEmpty(getDomain(orgId, adminPlatform)));
                log.info("Repeated Login Alarm: sendMail [{}], orgId => {}.", reply.getEmail(), orgId);
                messagePushClient.sendMail(orgId, reply.getEmail(), getRepeatedBusinessType(adminPlatform), LocaleUtil.getLanguage(), paramList);
            }
            if (StringUtils.isNotEmpty(reply.getAreaCode()) && StringUtils.isNotEmpty(reply.getTelephone())) {
                log.info("Repeated Login Alarm: sendSms [{} - {}], orgId => {}.", reply.getAreaCode(), reply.getTelephone(), orgId);
                messagePushClient.sendSms(orgId, reply.getAreaCode(), reply.getTelephone(), getRepeatedBusinessType(adminPlatform), LocaleUtil.getLanguage(), new ArrayList());
            }
        }
    }

    public void loginAlarm(Long orgId, Long userId) {
        AdminUserReply reply = adminUserClient.getAdminUserById(userId);
        if (Objects.nonNull(reply)) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss('GMT+08:00')");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String[] paramList = {Strings.nullToEmpty(dateFormat.format(new Date()))};
            if (StringUtils.isNotEmpty(reply.getEmail())) {
                String message = localeMessageService.getMessage(ADMIN_LOGIN_EMAIL_CONTENT, paramList);
                log.info("Admin Login Alarm: sendMail [{}], orgId => {}.", reply.getEmail(), orgId);
                //messagePushClient.sendMailDirectly(orgId, reply.getEmail(), "", "", message);
                messagePushClient.sendMail(orgId, reply.getEmail(), "", LocaleUtil.getLanguage(), Lists.newArrayList(message));
            }
            if (StringUtils.isNotEmpty(reply.getAreaCode()) && StringUtils.isNotEmpty(reply.getTelephone())) {
                String message = localeMessageService.getMessage(ADMIN_LOGIN_SMS_CONTENT, paramList);
                log.info("Admin Login Alarm: sendSms [{} - {}], orgId => {}.", reply.getAreaCode(), reply.getTelephone(), orgId);
                //messagePushClient.sendSmsDirectly(orgId, reply.getAreaCode(), reply.getTelephone(), message, "");
                messagePushClient.sendSms(orgId, reply.getAreaCode(), reply.getTelephone(), "", LocaleUtil.getLanguage(), Lists.newArrayList(message));
            }
        }
    }

    public String getRepeatedBusinessType(AdminPlatformEnum platform) {
        switch (platform) {
            case SAAS_ADMIN_PLATFROM: return SAAS_ADMIN_LOGIN_OTHER_PLACE;
            case BROKER_ADMIN_PLATFROM: return BROKER_ADMIN_LOGIN_OTHER_PLACE;
            case EXCHANGE_ADMIN_PLATFROM: return EX_ADMIN_LOGIN_OTHER_PLACE;
        }
        return null;
    }

    public String getDomain(Long orgId, AdminPlatformEnum platform) {
        switch (platform) {
            case SAAS_ADMIN_PLATFROM: return "";
            case BROKER_ADMIN_PLATFROM: return orgInstanceConfig.getAdminWebUrlByBrokerId(orgId);
            case EXCHANGE_ADMIN_PLATFROM: return orgInstanceConfig.getAdminWebUrlByExchangeId(orgId);
        }
        return null;
    }
}
