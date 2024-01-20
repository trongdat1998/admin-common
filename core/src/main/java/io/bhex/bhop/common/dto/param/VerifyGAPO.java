package io.bhex.bhop.common.dto.param;

import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.param
 * @Author: ming.xu
 * @CreateDate: 2019/3/19 4:56 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
public class VerifyGAPO {

    private Long orgId;

    private Long adminUserId;

    private Integer gaCode;
}
