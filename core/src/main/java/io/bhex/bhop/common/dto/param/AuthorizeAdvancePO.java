package io.bhex.bhop.common.dto.param;

import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.param
 * @Author: ming.xu
 * @CreateDate: 2019/3/29 3:44 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
public class AuthorizeAdvancePO {

    private Long orgId;

    private String requestId;

    private Integer authType;

    private String verifyCode;
}
