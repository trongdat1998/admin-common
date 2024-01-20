package io.bhex.bhop.common.grpc.client.impl;

import com.google.protobuf.TextFormat;
import io.bhex.base.account.*;
import io.bhex.bhop.common.config.GrpcConfig;
import io.bhex.bhop.common.grpc.client.BhAccountClient;
import io.bhex.bhop.common.util.BaseReqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description:平台账户绑定关系
 * @Date: 2018/10/8 下午7:08
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@Service
public class BhAccountClientImpl implements BhAccountClient {

    @Resource
    GrpcConfig grpcConfig;

    @Override
    public BindAccountReply bindAccount(long orgId, long accountId, AccountType accountType) {
        AccountServiceGrpc.AccountServiceBlockingStub stub = grpcConfig.accountServiceBlockingStub(GrpcConfig.SAAS_ADMIN_GRPC_CHANNEL_NAME);
        BindAccountRequest request = BindAccountRequest.newBuilder()
                .setOrgId(orgId)
                .setAccountId(accountId)
                .setAccountType(accountType)
                .build();
        return stub.bindAccount(request);
    }

    @Override
    public Long bindRelation(long orgId, AccountType accountType) {
        AccountServiceGrpc.AccountServiceBlockingStub stub = grpcConfig.accountServiceBlockingStub(GrpcConfig.SAAS_ADMIN_GRPC_CHANNEL_NAME);
        BindRelationRequest request = BindRelationRequest.newBuilder()
                .setOrgId(orgId)
                .setAccountType(accountType)
                .build();
        return stub.bindRelation(request).getAccountId();
    }

    @Override
    public Long getAccountBrokerId(Long accountId) {
        AccountServiceGrpc.AccountServiceBlockingStub stub = grpcConfig.accountServiceBlockingStub(GrpcConfig.SAAS_ADMIN_GRPC_CHANNEL_NAME);

        GetAccountInfoRequest request = GetAccountInfoRequest.newBuilder()
                .setAccountId(accountId)
                .build();

        GetAccountInfoReply reply = stub.getAccountInfo(request);
        log.info("request:{} response:{}", accountId, TextFormat.shortDebugString(reply));
        return reply.getOrgId();
    }

    @Override
    public Long getAccountBrokerId(Long accountId,Long orgId) {
        AccountServiceGrpc.AccountServiceBlockingStub stub = grpcConfig.accountServiceBlockingStub(GrpcConfig.SAAS_ADMIN_GRPC_CHANNEL_NAME);

        GetAccountInfoRequest request = GetAccountInfoRequest.newBuilder()
                .setBaseRequest(BaseReqUtil.getBaseRequest(orgId))
                .setAccountId(accountId)
                .build();

        GetAccountInfoReply reply = stub.getAccountInfo(request);
        log.info("request:{} response:{}", accountId, TextFormat.shortDebugString(reply));
        return reply.getOrgId();
    }
}
