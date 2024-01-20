package io.bhex.bhop.common.grpc.service;

import io.bhex.base.admin.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.bhop.common.service.IUserIpWhitelistService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.grpc.service
 * @Author: ming.xu
 * @CreateDate: 2019/3/20 2:02 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
@GrpcService
public class AdminUserIpWhitelistGrpcImpl extends AdminUserIpWhitelistServiceGrpc.AdminUserIpWhitelistServiceImplBase {

    @Autowired
    private IUserIpWhitelistService userIpWhitelistService;

    @Override
    public void addIpWhitelist(AddIpWhitelistRequest request, StreamObserver<OptionIpWhitelistResponse> responseObserver) {
        Boolean isOk = userIpWhitelistService.addIpWhitelist(request.getOrgId(), request.getAdminUserId(), request.getIpAddress());
        OptionIpWhitelistResponse response = OptionIpWhitelistResponse.newBuilder()
                .setRet(isOk)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteIpWhitelist(DeleteIpWhitelistRequest request, StreamObserver<OptionIpWhitelistResponse> responseObserver) {
        Boolean isOk = userIpWhitelistService.deleteIpWhitelist(request.getOrgId(), request.getAdminUserId(), request.getId());
        OptionIpWhitelistResponse response = OptionIpWhitelistResponse.newBuilder()
                .setRet(isOk)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void showIpWhitelist(ShowIpWhitelistRequest request, StreamObserver<ShowIpWhitelistResponse> responseObserver) {
        ShowIpWhitelistResponse response = userIpWhitelistService.showIpWhitelist(request.getOrgId());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
