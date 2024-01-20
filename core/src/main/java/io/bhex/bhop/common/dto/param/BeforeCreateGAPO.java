package io.bhex.bhop.common.dto.param;

import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.param
 * @Author: ming.xu
 * @CreateDate: 2019/3/19 4:49 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
public class BeforeCreateGAPO {

    private Long adminUserId;

    private Long orgId;

    private String accountName;

    private String orgName;
}
