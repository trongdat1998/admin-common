package io.bhex.bhop.common.grpc.service;

import io.bhex.base.admin.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.bhop.common.service.AdminUserSecurityService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.grpc.service
 * @Author: ming.xu
 * @CreateDate: 2019/3/14 2:44 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
@GrpcService
public class AdminUserSecurityGrpcImpl extends SecurityServiceGrpc.SecurityServiceImplBase {

    @Autowired
    private AdminUserSecurityService adminUserSecurityService;

    @Override
    public void beforeBindGA(SecurityBeforeBindGARequest request, StreamObserver<SecurityBeforeBindGAResponse> observer) {
        try {
            SecurityBeforeBindGAResponse response =
                    adminUserSecurityService.beforeBindGa(request.getOrgId(), request.getUserId(),
                            request.getGaIssuer(), request.getAccountName());
            observer.onNext(response);
            observer.onCompleted();
        } catch (Exception e) {
            log.error("security getGAKey error", e);
            observer.onError(new StatusRuntimeException(Status.UNKNOWN.withCause(e)));
        }
    }

    @Override
    public void bindGA(SecurityBindGARequest request, StreamObserver<SecurityBindGAResponse> observer) {
        try {
            SecurityBindGAResponse response =
                    adminUserSecurityService.bindGA(request.getOrgId(), request.getUserId(), request.getGaCode());
            observer.onNext(response);
            observer.onCompleted();
        } catch (Exception e) {
            log.error("security bindGA error", e);
            observer.onError(new StatusRuntimeException(Status.UNKNOWN.withCause(e)));
        }
    }

    @Override
    public void verifyGA(SecurityVerifyGARequest request, StreamObserver<SecurityVerifyGAResponse> observer) {
        try {
            SecurityVerifyGAResponse response =
                    adminUserSecurityService.verifyGA(request.getOrgId(), request.getUserId(), request.getGaCode());
            observer.onNext(response);
            observer.onCompleted();
        } catch (Exception e) {
            log.error("security verifyGA error", e);
            observer.onError(new StatusRuntimeException(Status.UNKNOWN.withCause(e)));
        }
    }

    @Override
    public void bindPhone(SecurityBindPhoneRequest request, StreamObserver<SecurityBindPhoneResponse> observer) {
        try {
            SecurityBindPhoneResponse response =
                    adminUserSecurityService.bindPhone(request.getOrgId(), request.getUserId(), request.getNationCode(),
                            request.getPhone());
            observer.onNext(response);
            observer.onCompleted();
        } catch (Exception e) {
            log.error("bind phone error", e);
            observer.onError(new StatusRuntimeException(Status.UNKNOWN.withCause(e)));
        }
    }
}
