package io.bhex.bhop.common.dto.param;

import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.param
 * @Author: ming.xu
 * @CreateDate: 11/12/2018 11:36 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class ListAllAuthByUserIdPO {

    private Long orgId;
    private Long userId;
}
