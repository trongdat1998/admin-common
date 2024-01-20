package io.bhex.bhop.common.grpc.service;

import io.bhex.base.admin.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.bhop.common.service.AdminUserAuthService;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.grpc.service
 * @Author: ming.xu
 * @CreateDate: 08/10/2018 11:01 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@GrpcService
public class AdminRoleAuthGrpcImpl extends AdminRoleAuthServiceGrpc.AdminRoleAuthServiceImplBase {

    @Autowired
    private AdminUserAuthService userAuthService;

    @Override
    public void addRole(AddRoleRequest request, StreamObserver<SaveRoleReply> responseObserver) {
        SaveRoleReply reply = userAuthService.addRole(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void updateRole(UpdateRoleRequest request, StreamObserver<SaveRoleReply> responseObserver) {
        SaveRoleReply reply = userAuthService.updateRole(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void enableRole(EnableRoleRequest request, StreamObserver<SaveRoleReply> responseObserver) {
        SaveRoleReply reply = userAuthService.enableRole(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void listRoleInfo(ListRoleInfoRequest request, StreamObserver<ListRoleInfoReply> responseObserver) {
        ListRoleInfoReply reply = userAuthService.listRoleInfo(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void listAuthPathInfo(ListAuthPathInfoRequest request, StreamObserver<ListAuthPathInfoReply> responseObserver) {
        ListAuthPathInfoReply reply = userAuthService.listAuthPathInfo(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void getRoleInfo(GetRoleInfoRequest request, StreamObserver<RoleInfo> responseObserver) {
        RoleInfo reply = userAuthService.getRoleInfo(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void listUserInfo(ListUserInfoRequest request, StreamObserver<ListUserInfoReply> responseObserver) {
        ListUserInfoReply reply = userAuthService.listUserInfo(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void listAllAuthByUserId(ListAllAuthByUserIdRequest request, StreamObserver<ListAllAuthByUserIdReply> responseObserver) {
        ListAllAuthByUserIdReply reply = userAuthService.listAllAuthByUserId(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void saveUserRole(SaveUserRoleRequest request, StreamObserver<SaveUserRoleReply> responseObserver) {
        Boolean isOk = userAuthService.saveUserRole(request.getOrgId(), request.getUserId(), request.getRoleIdsList());
        SaveUserRoleReply reply = SaveUserRoleReply.newBuilder()
                .setResult(isOk)
                .build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void listAuthPathId(ListAuthPathIdRequest request, StreamObserver<ListAuthPathIdReply> responseObserver) {
        ListAuthPathIdReply reply = userAuthService.listAuthPathId(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void listAllAuthByRoleId(ListAllAuthByRoleIdRequest request, StreamObserver<ListAllAuthByRoleIdReply> responseObserver) {
        ListAllAuthByRoleIdReply reply = userAuthService.listAllAuthByRoleId(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void listAllAuthId(ListAllAuthIdRequest request, StreamObserver<ListAllAuthIdReply> responseObserver) {
        ListAllAuthIdReply reply = userAuthService.listAllAuthId(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
