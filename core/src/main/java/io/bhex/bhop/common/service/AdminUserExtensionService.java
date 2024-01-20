package io.bhex.bhop.common.service;

import io.bhex.bhop.common.dto.param.CreateAdminUserPO;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service
 * @Author: ming.xu
 * @CreateDate: 16/09/2018 3:26 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public interface AdminUserExtensionService {

    void afterCreateUser(CreateAdminUserPO userPO);

}
