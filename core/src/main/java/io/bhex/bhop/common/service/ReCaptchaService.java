package io.bhex.bhop.common.service;

import io.bhex.broker.common.api.client.geetest.DkGeeTestApi;
import io.bhex.broker.common.api.client.geetest.DkGeeTestVerifyRequest;
import io.bhex.broker.common.api.client.geetest.v3.DKGeeTestV3Api;
import io.bhex.broker.common.api.client.geetest.v3.sdk.GeetestLibResult;
import io.bhex.broker.common.api.client.recaptcha.GoogleRecaptchaApi;
import io.bhex.broker.common.api.client.recaptcha.GoogleRecaptchaRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.service.impl
 * @Author: ming.xu
 * @CreateDate: 21/11/2018 6:10 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class ReCaptchaService {

    @Resource
    private DkGeeTestApi dkGeeTestApi;

    @Resource
    private GoogleRecaptchaApi googleRecaptchaApi;

    @Value("${re-captcha-supplier:none}")
    private String reCaptchaSupplier;

    @Resource
    private DKGeeTestV3Api dkGeeTestV3Api;

    private static final String ADMIN_CLIENT_TYPE = "web";

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder().build();

    private void validGoogleReCaptchaResponse(String remoteIp, String responseCode) {
        GoogleRecaptchaRequest recaptchaRequest = GoogleRecaptchaRequest.builder()
                .recaptchaResponse(responseCode)
                .remoteIp(remoteIp)
                .build();
        googleRecaptchaApi.validGoogleReCaptchaResponse(recaptchaRequest);
    }

    public void validReCaptcha(HttpServletRequest request, String captchaId, String challenge, String captchaResponse, String scene, String userId) {
        if ("gee".equalsIgnoreCase(reCaptchaSupplier)) {
            validGeeTestReCaptchaResponse(captchaId, captchaResponse, scene, userId);
        } else if ("google".equalsIgnoreCase(reCaptchaSupplier)) {
            String ip = getIPAddress(request);
            validGoogleReCaptchaResponse(ip, captchaResponse);
        } else if ("geeV3".equalsIgnoreCase(reCaptchaSupplier)) {
            validGeeTestV3Captcha(captchaId, challenge, captchaResponse);
        } else if (!"none".equalsIgnoreCase(reCaptchaSupplier)) {
            log.error("validGeeTestCaptcha error！error config！{}", reCaptchaSupplier);
        }
    }

    public GeetestLibResult registerGeeV3(String geeId) {
        if ("geeV3".equalsIgnoreCase(reCaptchaSupplier)) {
            return dkGeeTestV3Api.registerGeeV3(geeId, "admin", ADMIN_CLIENT_TYPE);
        } else {
            log.error("validGeeTestCaptcha error！error config！{}", reCaptchaSupplier);
            return null;
        }
    }

    private void validGeeTestV3Captcha(String captchaId, String challenge, String captchaResponse) {
        dkGeeTestV3Api.validGeeTestCaptcha(captchaId, challenge, captchaResponse);
    }

    private void validGeeTestReCaptchaResponse(String captchaId, String sessionId, String scene, String userId) {
        DkGeeTestVerifyRequest verifyRequest = DkGeeTestVerifyRequest.builder()
                .id(captchaId)
                .sessionId(sessionId)
                .scene(scene)
                .userId(userId)
                .build();
        dkGeeTestApi.validGeeTestCaptcha(verifyRequest);
    }

    private String getIPAddress(HttpServletRequest request) {
        String ip = null;
        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
        }
        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }
        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
