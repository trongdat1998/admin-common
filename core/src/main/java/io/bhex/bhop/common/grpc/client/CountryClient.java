package io.bhex.bhop.common.grpc.client;

import io.bhex.base.admin.common.QueryCountryResponse;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.grpc.client
 * @Author: ming.xu
 * @CreateDate: 2019/4/10 4:57 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public interface CountryClient {

    QueryCountryResponse queryCountries();
}
