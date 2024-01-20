package io.bhex.bhop.common.grpc.client.impl;

import io.bhex.base.admin.*;
import io.bhex.bhop.common.config.GrpcConfig;
import io.bhex.bhop.common.grpc.client.UserIpWhitelistServiceClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.grpc.client.impl
 * @Author: ming.xu
 * @CreateDate: 2019/3/20 4:16 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Service
public class UserIpWhitelistServiceClientImpl implements UserIpWhitelistServiceClient {

    @Resource
    GrpcConfig grpcConfig;

    private AdminUserIpWhitelistServiceGrpc.AdminUserIpWhitelistServiceBlockingStub getIpWhitelistStub() {
        return grpcConfig.adminUserIpWhitelistServiceBlockingStub(GrpcConfig.ADMIN_COMMON_GRPC_CHANNEL_NAME);
    }

    @Override
    public OptionIpWhitelistResponse addIpWhitelist(AddIpWhitelistRequest request) {
        return getIpWhitelistStub().addIpWhitelist(request);
    }

    @Override
    public OptionIpWhitelistResponse deleteIpWhitelist(DeleteIpWhitelistRequest request) {
        return getIpWhitelistStub().deleteIpWhitelist(request);
    }

    @Override
    public ShowIpWhitelistResponse showIpWhitelist(ShowIpWhitelistRequest request) {
        return getIpWhitelistStub().showIpWhitelist(request);
    }
}
