package io.bhex.bhop.common.grpc.service;

import io.bhex.base.admin.common.CountryServiceGrpc;
import io.bhex.base.admin.common.QueryCountryRequest;
import io.bhex.base.admin.common.QueryCountryResponse;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.bhop.common.service.ICountryService;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.grpc.service
 * @Author: ming.xu
 * @CreateDate: 2019/4/10 4:37 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@GrpcService
public class CountrGrpcImpl extends CountryServiceGrpc.CountryServiceImplBase {

    @Autowired
    private ICountryService countryService;

    @Override
    public void queryCountries(QueryCountryRequest request, StreamObserver<QueryCountryResponse> responseObserver) {
        QueryCountryResponse response = countryService.queryCountries();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
