package io.bhex.bhop.common.service;

import io.bhex.base.admin.*;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service
 * @Author: ming.xu
 * @CreateDate: 2019/3/14 2:44 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public interface AdminUserSecurityService {

    SecurityBeforeBindGAResponse beforeBindGa(Long orgId, Long userId, String gaIssuer, String accountName);

    SecurityBindGAResponse bindGA(Long orgId, Long userId, Integer gaCode);

    SecurityVerifyGAResponse verifyGA(Long orgId, Long userId, Integer gaCode);

    SecurityBindPhoneResponse bindPhone(Long orgId, Long userId, String nationalCode, String phone);
}
