package io.bhex.bhop.common.grpc.client.impl;

import io.bhex.base.admin.common.CountryServiceGrpc;
import io.bhex.base.admin.common.QueryCountryRequest;
import io.bhex.base.admin.common.QueryCountryResponse;
import io.bhex.bhop.common.config.GrpcConfig;
import io.bhex.bhop.common.grpc.client.CountryClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.grpc.client.impl
 * @Author: ming.xu
 * @CreateDate: 2019/4/10 5:03 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Service
public class CountryClientImpl implements CountryClient {

    @Resource
    GrpcConfig grpcConfig;

    private CountryServiceGrpc.CountryServiceBlockingStub getCountryStub(){
        return grpcConfig.countryServiceBlockingStub(GrpcConfig.ADMIN_COMMON_GRPC_CHANNEL_NAME);
    }

    @Override
    public QueryCountryResponse queryCountries() {
        QueryCountryResponse response = getCountryStub().queryCountries(QueryCountryRequest.newBuilder().build());
        return response;
    }
}
