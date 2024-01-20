package io.bhex.bhop.common.grpc.client.impl;

import io.bhex.base.admin.*;
import io.bhex.base.admin.SecurityServiceGrpc;
import io.bhex.bhop.common.config.GrpcConfig;
import io.bhex.bhop.common.grpc.client.AdminUserSecurityClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.grpc.client.impl
 * @Author: ming.xu
 * @CreateDate: 2019/3/15 6:17 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Service
public class AdminUserSecurityClientImpl implements AdminUserSecurityClient {

    @Resource
    GrpcConfig grpcConfig;

    private SecurityServiceGrpc.SecurityServiceBlockingStub getSecurityStub() {
        return grpcConfig.securityServiceBlockingStub(GrpcConfig.ADMIN_COMMON_GRPC_CHANNEL_NAME);
    }

    @Override
    public SecurityBeforeBindGAResponse beforeBindGA(SecurityBeforeBindGARequest request) {
        return getSecurityStub().beforeBindGA(request);
    }

    @Override
    public SecurityBindGAResponse bindGA(SecurityBindGARequest request) {
        return getSecurityStub().bindGA(request);
    }

    @Override
    public SecurityVerifyGAResponse verifyGA(SecurityVerifyGARequest request) {
        return getSecurityStub().verifyGA(request);
    }

    @Override
    public SecurityBindPhoneResponse bindPhone(SecurityBindPhoneRequest request) {
        return getSecurityStub().bindPhone(request);
    }
}
