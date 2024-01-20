package io.bhex.bhop.common.controller;


import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.base.admin.common.BusinessLog;
import io.bhex.base.admin.common.SaveLogRequest;
import io.bhex.bhop.common.config.LocaleMessageService;
import io.bhex.bhop.common.enums.AdminPlatformEnum;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.grpc.client.BusinessLogClient;
import io.bhex.bhop.common.jwt.authorize.Authorize;
import io.bhex.bhop.common.jwt.authorize.JwtTokenProvider;
import io.bhex.bhop.common.service.BaseCommonService;
import io.bhex.bhop.common.service.ReCaptchaService;
import io.bhex.bhop.common.util.RequestUtil;
import io.bhex.bhop.common.util.ResultModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BaseController {

    public static final Integer GEE_TEST_MOBILE_ID_TYPE = 1;
    public static final Integer GEE_TEST_EMAIL_ID_TYPE = 2;

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private AdminUserClient adminUserClient;

    @Resource
    private BaseCommonService baseCommonService;

    @Resource
    protected ReCaptchaService reCaptchaService;

    @Resource
    private BusinessLogClient businessLogClient;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Resource
    private LocaleMessageService localeMessageService;

    protected ResultModel errorFieldOutput(String field, String message) {
        String fieldVal = localeMessageService.getMessage("field." + field);
        if (fieldVal == null || fieldVal.equals("field." + field)) {
            fieldVal = field;
        }
        return ResultModel.error(fieldVal + ":" + localeMessageService.getMessage(message));
    }

    protected boolean opFrequently(String key, long expireTimeInSeconds) {
        String realKey = "OPERATION_" + key;
        String lastOpTimeStr = redisTemplate.opsForValue().get(realKey);
        if (StringUtils.isEmpty(lastOpTimeStr)) {
            redisTemplate.opsForValue().set(realKey, System.currentTimeMillis() + "",
                    expireTimeInSeconds, TimeUnit.SECONDS);
            return false;
        }
        return true;
    }

    protected boolean cancelOpFrequently(String key) {
        String realKey = "OPERATION_" + key;
        return redisTemplate.delete(realKey);
    }

    public Long getRequestUserId() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String jwtToken = RequestUtil.getCookieValue(request, Authorize.COOKIE_TOKEN);
            String subject = jwtTokenProvider.parseSubject(jwtToken);
            if (StringUtils.isEmpty(subject)) {
                return null;
            }
            Long userId = Long.valueOf(subject);

            if (Optional.ofNullable(userId).isPresent()) {
                request.setAttribute(Authorize.ATTRIBUTE_USER_ID, userId);
                MDC.put("userId", Objects.toString(userId));
            }

            return userId;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("getRequestUser error", e);
        }
        return null;
    }

    public AdminUserReply getRequestUser() {

        Long id = getRequestUserId();
        if (id == null) {
            throw new BizException(ErrorCode.LOGIN_TOKEN_ERROR, "need.login");
        }
        AdminUserReply reply = adminUserClient.getAdminUserById(id);
        return reply;
    }

    public AdminPlatformEnum getAdminPlatform() {
        return baseCommonService.getAdminPlatform();
    }

    public Long getOrgId() {
        return baseCommonService.getOrgId();
    }

    public String getRrmoteIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return baseCommonService.getRemoteIp(request);
    }



    /**
     * int64 org_id = 1;
     *     string username = 2;
     *     string op_type = 3;
     *     int32  result_code = 4;
     *     string result_msg = 5;
     *     string remark = 6;
     *     string entity_id = 7;
     *     string request_info = 8;
     *     string ip = 9;
     * @param opType
     */
    public void saveBizLog(String opType, String opContent, String requestInfo, int resultCode) {
        try {
            saveBizLog(opType, null, opContent, requestInfo, resultCode, "");
        } catch (Exception e) {
            log.warn("save biz log error", e);
        }
    }

    public void saveBizLog(String opType, String opContent, String requestInfo, int resultCode, String entityId) {
        try {
            saveBizLog(opType, null, opContent, requestInfo, resultCode, entityId);
        } catch (Exception e) {
            log.warn("save biz log error", e);
        }
    }


    public void saveBizLog(String opType, String subType, String opContent, String requestInfo, int resultCode, String entityId) {
        AdminUserReply reply = getRequestUser();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String referer = request.getHeader("referer") == null ? "" : request.getHeader("referer");
        BusinessLog.Builder logBuilder = BusinessLog.newBuilder();
        logBuilder.setOrgId(reply.getOrgId())
                .setUsername(reply.getUsername())
                .setOpType(opType)
                .setSubType(Strings.nullToEmpty(subType))
                .setRemark(Strings.nullToEmpty(opContent))
                .setRequestInfo(requestInfo)
                .setRequestUrl(referer)
                .setEntityId(Strings.nullToEmpty(entityId))
                .setIp(getRrmoteIp())
                .setUserAgent(Strings.nullToEmpty(request.getHeader("user-agent")))
                .setVisible(true)
                .setResultCode(resultCode)
                .build();
        businessLogClient.saveLog(SaveLogRequest.newBuilder().setBusinessLog(logBuilder.build()).build());
    }

    public String parseDomain(HttpServletRequest request) {
        if (Objects.isNull(request)) {
            return "";
        }
        String url = request.getScheme() + "://" + request.getServerName();
        if (request.getServerPort() == 80) {
            return url;
        }

        return url + ":" + request.getServerPort();
    }


}
