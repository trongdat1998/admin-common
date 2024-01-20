package io.bhex.bhop.common.grpc.client;

import io.bhex.base.admin.*;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.grpc.client
 * @Author: ming.xu
 * @CreateDate: 2019/3/15 6:16 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public interface AdminUserSecurityClient {

    SecurityBeforeBindGAResponse beforeBindGA(SecurityBeforeBindGARequest request);

    SecurityBindGAResponse bindGA(SecurityBindGARequest request);

    SecurityVerifyGAResponse verifyGA(SecurityVerifyGARequest request);

    SecurityBindPhoneResponse bindPhone(SecurityBindPhoneRequest request);
}
