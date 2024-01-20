package io.bhex.bhop.common.grpc.client;

import io.bhex.base.admin.*;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.grpc.client
 * @Author: ming.xu
 * @CreateDate: 2019/3/20 4:15 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public interface UserIpWhitelistServiceClient {

    OptionIpWhitelistResponse addIpWhitelist(AddIpWhitelistRequest request);

    OptionIpWhitelistResponse deleteIpWhitelist(DeleteIpWhitelistRequest request);

    ShowIpWhitelistResponse showIpWhitelist(ShowIpWhitelistRequest request);

}
