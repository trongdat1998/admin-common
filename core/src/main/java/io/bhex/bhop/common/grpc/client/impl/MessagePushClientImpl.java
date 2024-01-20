package io.bhex.bhop.common.grpc.client.impl;

import io.bhex.base.common.*;
import io.bhex.bhop.common.config.GrpcConfig;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.grpc.client.MessagePushClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Service
public class MessagePushClientImpl implements MessagePushClient {

    @Resource
    GrpcConfig grpcConfig;

    @Value("${verify-captcha:true}")
    private Boolean verifyCaptcha;

    @Value("${global-notify-type:1}")
    private Integer globalNotifyType;

    @Override
    public void sendMail(Long orgId, String mailAddress, String businessType, String language, List<String> params) {
        sendMail(orgId, 0L, mailAddress, businessType, language, params);
    }

    @Override
    public void sendMail(Long orgId, Long userId, String mailAddress, String businessType, String language, List<String> params) {
        log.info("{} {} {}", orgId, mailAddress, businessType);
        if (verifyCaptcha && globalNotifyType == 2)
            throw new BizException(ErrorCode.NO_PERMISSION, "Only support phone!");
        MessageServiceGrpc.MessageServiceBlockingStub stub = grpcConfig.messageServiceBlockingStub(GrpcConfig.COMMON_SERVER_CHANNEL_NAME);
        SimpleMailRequest builder = SimpleMailRequest.newBuilder()
                .setOrgId(orgId)
                .setMail(mailAddress)
                .addAllParams(params != null ? params : new ArrayList<>())
                .setBusinessType(businessType)
                .setLanguage(language)
                .setUserId(userId)
                .build();

        MessageReply reply = stub.sendSimpleMail(builder);
        log.info("reply:{}", reply);
    }

    @Override
    public void sendSms(Long orgId, String nationalCode, String mobile, String businessType, String language, List<String> params) {
        sendSms(orgId, 0L, nationalCode, mobile, businessType, language, params);
    }

    @Override
    public void sendSms(Long orgId, Long userId, String nationalCode, String mobile, String businessType, String language, List<String> params) {
        log.info("{} {} {}", orgId, mobile, businessType);
        if (verifyCaptcha && globalNotifyType == 3)
            throw new BizException(ErrorCode.NO_PERMISSION, "Only support email!");
        MessageServiceGrpc.MessageServiceBlockingStub stub = grpcConfig.messageServiceBlockingStub(GrpcConfig.COMMON_SERVER_CHANNEL_NAME);
        SimpleSMSRequest builder = SimpleSMSRequest.newBuilder()
                .setOrgId(orgId)
                .setTelephone(Telephone.newBuilder().setNationCode(nationalCode).setMobile(mobile).build())
                .addAllParams(params != null ? params : new ArrayList<>())
                .setBusinessType(businessType)
                .setLanguage(language)
                .setUserId(userId)
                .build();

        MessageReply reply = stub.sendSimpleSMS(builder);
        log.info("reply:{}", reply);
    }

    @Override
    public void sendMailDirectly(Long orgId, String mailAddress, String subject, String sign, String content, String language) {
        log.info("{} {} {}", orgId, mailAddress, subject);
        if (verifyCaptcha && globalNotifyType == 2)
            throw new BizException(ErrorCode.NO_PERMISSION, "Only support phone!");
        MessageServiceGrpc.MessageServiceBlockingStub stub = grpcConfig.messageServiceBlockingStub(GrpcConfig.COMMON_SERVER_CHANNEL_NAME);
        SimpleMailRequest builder = SimpleMailRequest.newBuilder()
                .setOrgId(orgId)
                .setMail(mailAddress)
                .addAllParams(Arrays.asList(new String[]{content}))
                .setEmailSubject(StringUtils.isEmpty(subject) ? "" : subject)
                .setLanguage(language)
                .build();

        MessageReply reply = stub.sendSimpleMail(builder);
        log.info("reply:{}", reply);
    }

    @Override
    public void sendSmsDirectly(Long orgId, String nationalCode, String mobile, String content, String sign, String language) {
        log.info("{} {} {}", orgId, mobile, sign);
        if (verifyCaptcha && globalNotifyType == 3)
            throw new BizException(ErrorCode.NO_PERMISSION, "Only support email!");
        MessageServiceGrpc.MessageServiceBlockingStub stub = grpcConfig.messageServiceBlockingStub(GrpcConfig.COMMON_SERVER_CHANNEL_NAME);
        SimpleSMSRequest builder = SimpleSMSRequest.newBuilder()
                .setOrgId(orgId)
                .setTelephone(Telephone.newBuilder().setNationCode(nationalCode).setMobile(mobile).build())
                .addAllParams(Arrays.asList(new String[]{content}))
                .setSign(StringUtils.isEmpty(sign) ? "" : sign)
                .setLanguage(language)
                .build();

        MessageReply reply = stub.sendSimpleSMS(builder);
        log.info("reply:{}", reply);
    }

    @Override
    public void sendVerificationCodeSmsDirectly(Long orgId, String nationalCode, String mobile, String code, String language ) {
        log.info("{} {}", orgId, mobile);
        if (verifyCaptcha && globalNotifyType == 3)
            throw new BizException(ErrorCode.NO_PERMISSION, "Only support email!");
        //SET_PASSWORD的提示比较中性，可以作为通用的验证码发送模版
        MessageServiceGrpc.MessageServiceBlockingStub stub = grpcConfig.messageServiceBlockingStub(GrpcConfig.COMMON_SERVER_CHANNEL_NAME);
        SimpleSMSRequest builder = SimpleSMSRequest.newBuilder()
                .setOrgId(orgId)
                .setBusinessType("SET_PASSWORD")
                .setTelephone(Telephone.newBuilder().setNationCode(nationalCode).setMobile(mobile).build())
                .addParams(code)
                .putReqParam("code", code)
                .setLanguage(language)
                .build();
        MessageReply reply = stub.sendSimpleSMS(builder);
        log.info("reply:{}", reply);
    }
}
