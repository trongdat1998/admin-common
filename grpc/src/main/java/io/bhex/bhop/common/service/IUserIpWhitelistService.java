package io.bhex.bhop.common.service;

import io.bhex.base.admin.ShowIpWhitelistResponse;
import io.bhex.bhop.common.entity.UserIpWhitelist;

import java.util.List;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service
 * @Author: ming.xu
 * @CreateDate: 2019/3/19 11:32 AM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public interface IUserIpWhitelistService {

     Boolean addIpWhitelist(Long orgId, Long admingId, String ipAddress);

     Boolean deleteIpWhitelist(Long orgId, Long admingId, Long id);

     ShowIpWhitelistResponse showIpWhitelist(Long orgId);
}
