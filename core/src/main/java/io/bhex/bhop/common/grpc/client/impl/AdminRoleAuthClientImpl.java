package io.bhex.bhop.common.grpc.client.impl;

import io.bhex.base.admin.*;
import io.bhex.bhop.common.config.GrpcConfig;
import io.bhex.bhop.common.grpc.client.AdminRoleAuthClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.grpc.client.impl
 * @Author: ming.xu
 * @CreateDate: 09/10/2018 4:55 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Service
public class AdminRoleAuthClientImpl implements AdminRoleAuthClient {

    @Resource
    GrpcConfig grpcConfig;

    private AdminRoleAuthServiceGrpc.AdminRoleAuthServiceBlockingStub getRoleStub() {
        return grpcConfig.adminRoleAuthServiceBlockingStub(GrpcConfig.ADMIN_COMMON_GRPC_CHANNEL_NAME);
    }

    @Override
    public SaveRoleReply addRole(AddRoleRequest request) {
        return getRoleStub().addRole(request);
    }

    @Override
    public SaveRoleReply updateRole(UpdateRoleRequest request) {
        return getRoleStub().updateRole(request);
    }

    @Override
    public ListRoleInfoReply listRoleInfo(ListRoleInfoRequest request) {
        return getRoleStub().listRoleInfo(request);
    }

    @Override
    public ListUserInfoReply listUserInfo(ListUserInfoRequest request) {
        return getRoleStub().listUserInfo(request);
    }

    @Override
    public RoleInfo getRoleInfo(GetRoleInfoRequest request) {
        return getRoleStub().getRoleInfo(request);
    }

    @Override
    public SaveUserRoleReply saveUserRole(SaveUserRoleRequest request) {
        return getRoleStub().saveUserRole(request);
    }

    @Override
    public SaveRoleReply enableRole(EnableRoleRequest request) {
        return getRoleStub().enableRole(request);
    }

    @Override
    public ListAuthPathInfoReply listAuthPathInfo(ListAuthPathInfoRequest request) {
        return getRoleStub().listAuthPathInfo(request);
    }

    @Override
    public ListAllAuthByUserIdReply listAllAuthByUserId(ListAllAuthByUserIdRequest request) {
        return getRoleStub().listAllAuthByUserId(request);
    }

    @Override
    public ListAllAuthByRoleIdReply listAllAuthByRoleId(ListAllAuthByRoleIdRequest request) {
        return getRoleStub().listAllAuthByRoleId(request);
    }

    @Override
    public ListAllAuthIdReply listAllAuthId(ListAllAuthIdRequest request) {
        return getRoleStub().listAllAuthId(request);
    }
}
