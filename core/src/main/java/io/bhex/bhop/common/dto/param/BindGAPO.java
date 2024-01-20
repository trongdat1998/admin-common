package io.bhex.bhop.common.dto.param;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.param
 * @Author: ming.xu
 * @CreateDate: 2019/3/19 4:51 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
public class BindGAPO {

    private Long adminUserId;

    private Long orgId;

    private String email;

    @NotEmpty
    private Integer gaCode;

    @NotEmpty
    private String verifyCode;
}
