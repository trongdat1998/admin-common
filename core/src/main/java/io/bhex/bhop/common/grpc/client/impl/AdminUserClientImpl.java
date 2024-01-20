package io.bhex.bhop.common.grpc.client.impl;


import com.google.protobuf.TextFormat;
import io.bhex.base.admin.common.*;
import io.bhex.bhop.common.config.GrpcConfig;
import io.bhex.bhop.common.config.OrgInstanceConfig;
import io.bhex.bhop.common.dto.param.BrokerInstanceRes;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
public class AdminUserClientImpl implements AdminUserClient {

    @Resource
    private GrpcConfig grpcConfig;

    @Autowired
    private OrgInstanceConfig orgInstanceConfig;

    private AdminUserServiceGrpc.AdminUserServiceBlockingStub getAdminStub() {
        return grpcConfig.adminUserServiceBlockingStub(GrpcConfig.ADMIN_COMMON_GRPC_CHANNEL_NAME);
    }


    @Override
    public LoginByUsernameAndPasswordReply loginByUsernameAndPassword(Long orgId, String username, String password) {
        LoginByUsernameAndPasswordRequest request = LoginByUsernameAndPasswordRequest.newBuilder()
                .setOrgId(orgId)
                .setUsername(username)
                .setPassword(password)
                .build();
        LoginByUsernameAndPasswordReply reply = getAdminStub().loginByUsernameAndPassword(request);
        log.info("login reply:{}", TextFormat.shortDebugString(reply));
        return reply;
    }

    @Override
    public int countByUsername(String username) {
        CountByUsernameRequest request = CountByUsernameRequest.newBuilder().setUsername(username).build();
        CountByUsernameReply reply = getAdminStub().countByUsername(request);
        return reply.getCount();
    }

    @Override
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        if (StringUtils.isNotEmpty(oldPassword) && StringUtils.isNotEmpty(newPassword)) {
            ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
                    .setId(id)
                    .setNewPassword(newPassword)
                    .setOldPassword(oldPassword)
                    .build();
            ChangePasswordReply changePasswordReply = getAdminStub().changePassword(request);
            if (!changePasswordReply.getResult()) {
                throw new BizException(ErrorCode.WRONG_PASSWORD);
            }
            return changePasswordReply.getResult();
        }
        return false;
    }

    @Override
    public boolean sendChangePasswordEmail(Long id) {
        return false;
    }

    @Override
    public AddAdminUserReply addAdminUser(AddAdminUserRequest request) {
        AddAdminUserReply reply = getAdminStub().addAdminUser(request);
        return reply;
    }

    @Override
    public AddAdminUserReply addSubAdminUser(AddSubAdminUserRequest request) {
        AddAdminUserReply reply = getAdminStub().addSubAdminUser(request);
        return reply;
    }

    @Override
    public UpdateSubAdminUserReply updateSubAdminUser(UpdateSubAdminUserRequest request) {
        UpdateSubAdminUserReply reply = getAdminStub().updateSubAdminUser(request);
        return reply;
    }

    @Override
    public EnableSubAdminUserReply enableSubAdminUser(EnableSubAdminUserRequest request) {
        EnableSubAdminUserReply reply = getAdminStub().enableSubAdminUser(request);
        return reply;
    }

    @Override
    public AdminUserReply getAdminUserById(Long id){
        GetAdminUserByIdRequest request = GetAdminUserByIdRequest.newBuilder().setId(id).build();
        AdminUserReply reply = getAdminStub().getAdminUserById(request);
        BrokerInstanceRes brokerInstanceRes = orgInstanceConfig.getBrokerInstance(reply.getOrgId());
        if (brokerInstanceRes != null) {
            reply = reply.toBuilder().setOrgName(brokerInstanceRes.getBrokerName()).build();
        }
        return reply;
    }

    @Override
    public AdminUserReply getAdminUserById(Long id, Long orgId) {
        GetAdminUserByIdAndOrgIdRequest request = GetAdminUserByIdAndOrgIdRequest.newBuilder()
                .setId(id)
                .setOrgId(orgId)
                .build();
        AdminUserReply reply = getAdminStub().getAdminUserByIdAndOrgId(request);
        BrokerInstanceRes brokerInstanceRes = orgInstanceConfig.getBrokerInstance(reply.getOrgId());
        if (brokerInstanceRes != null) {
            reply = reply.toBuilder().setOrgName(brokerInstanceRes.getBrokerName()).build();
        }
        return reply;
    }

    @Override
    public GetInitPasswordTokenReply getInitPasswordToken(Long adminUserId) {
        GetInitPasswordTokenRequest request = GetInitPasswordTokenRequest.newBuilder()
                .setAdminUserId(adminUserId)
                .build();
        GetInitPasswordTokenReply reply = getAdminStub().getInitPasswordToken(request);

        return reply;
    }


    @Override
    public String saveInitPasswordToken(Long adminUserId) {
        String token = MD5Util.getMD5(String.valueOf(adminUserId + System.currentTimeMillis()));
        //save token
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        //expire date +1 day
        calendar.add(calendar.DATE,1);
        SaveInitPasswordTokenRequest tokenRequest = SaveInitPasswordTokenRequest.newBuilder()
                .setAdminUserId(adminUserId)
                .setToken(token)
                .setExpiredAt(calendar.getTimeInMillis())
                .build();
        SaveInitPasswordTokenReply reply = getAdminStub().saveInitPasswordToken(tokenRequest);

        return reply.getResult() == true ? token : null;
    }


    @Override
    public boolean saveInitPassword(Long adminUserId, String password) {
        SaveInitPasswordRequest request = SaveInitPasswordRequest.newBuilder()
                .setAdminUserId(adminUserId)
                .setPassword(password)
                .build();

        return getAdminStub().saveInitPassword(request).getResult();
    }

    @Override
    public boolean resetPassword(Long adminUserId, String password) {
        ResetPasswordRequest request = ResetPasswordRequest.newBuilder()
                .setAdminUserId(adminUserId)
                .setPassword(password)
                .build();

        return getAdminStub().resetPassword(request).getResult();
    }

    @Override
    public AdminUserReply getAdminUserByEmail(String email, Long orgId) {
        GetAdminUserByEmailRequest request = GetAdminUserByEmailRequest.newBuilder()
                .setOrgId(orgId)
                .setEmail(email)
                .build();

        AdminUserReply reply = getAdminStub().getAdminUserByEmail(request);
        BrokerInstanceRes brokerInstanceRes = orgInstanceConfig.getBrokerInstance(reply.getOrgId());
        if (brokerInstanceRes != null) {
            reply = reply.toBuilder().setOrgName(brokerInstanceRes.getBrokerName()).build();
        }
        return reply;
    }

    @Override
    public AdminUserReply getAdminRootUserByOrgId(Long orgId) {
        GetAdminUserByOrgIdRequest request = GetAdminUserByOrgIdRequest.newBuilder()
                .setOrgId(orgId)
                .build();
        AdminUserReply reply = getAdminStub().getAdminUserByOrgId(request);
        BrokerInstanceRes brokerInstanceRes = orgInstanceConfig.getBrokerInstance(reply.getOrgId());
        if (brokerInstanceRes != null) {
            reply = reply.toBuilder().setOrgName(brokerInstanceRes.getBrokerName()).build();
        }
        return reply;
    }

    @Override
    public List<AdminUserReply> getAdminUserByUids(Long orgId, List<Long> uidList) {
        GetAdminUserByUidsRequest request = GetAdminUserByUidsRequest.newBuilder()
                .setOrgId(orgId)
                .addAllUid(uidList)
                .build();
        return getAdminStub().getAdminUserByUids(request).getUserListList();
    }

    @Override
    public List<Long> getOrgIds() {
        return getAdminStub().getOrgIds(GetOrgIdsRequest.newBuilder().build()).getOrgIdList();
    }

    @Override
    public AdminUserReply getAdminUserByPhone(String nationalCode, String phone, Long orgId) {
        AdminUserReply reply = getAdminStub().getAdminUserByPhone(
                GetAdminUserByPhoneRequest.newBuilder()
                        .setNationCode(nationalCode)
                        .setPhone(phone)
                        .setOrgId(orgId)
                        .build()
        );
        BrokerInstanceRes brokerInstanceRes = orgInstanceConfig.getBrokerInstance(reply.getOrgId());
        if (brokerInstanceRes != null) {
            reply = reply.toBuilder().setOrgName(brokerInstanceRes.getBrokerName()).build();
        }
        return reply;
    }

    @Override
    public Boolean isPhoneExist(String nationalCode, String phone, Long orgId, Long userId) {
        if (StringUtils.isEmpty(phone)) {
            return false;
        }
        AdminUserReply adminUserByPhone = getAdminUserByPhone(nationalCode, phone, orgId);
        if (Objects.nonNull(adminUserByPhone) && adminUserByPhone.getId() != 0 && userId != adminUserByPhone.getId()) {
            return true;
        }
        return false;
    }

    @Override
    public ChangeAdminUserReply changeAdminUser(ChangeAdminUserRequest request) {
        return getAdminStub().changeAdminUser(request);
    }

    @Override
    public List<AdminUserReply> listAdminUserByOrgId(Long orgId) {
        GetAdminUserByOrgIdRequest request = GetAdminUserByOrgIdRequest.newBuilder()
                .setOrgId(orgId)
                .build();
        return getAdminStub().getAdminUserListByOrgId(request).getUserListList();
    }
}
