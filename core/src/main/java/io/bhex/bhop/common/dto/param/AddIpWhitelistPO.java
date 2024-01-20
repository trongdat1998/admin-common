package io.bhex.bhop.common.dto.param;

import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.param
 * @Author: ming.xu
 * @CreateDate: 2019/3/20 4:41 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
public class AddIpWhitelistPO {

    private Long orgId;

    private Long adminId;

    private String ipAddress;

}
