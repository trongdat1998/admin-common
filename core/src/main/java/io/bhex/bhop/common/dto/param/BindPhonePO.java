package io.bhex.bhop.common.dto.param;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.param
 * @Author: ming.xu
 * @CreateDate: 2019/4/3 3:55 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
public class BindPhonePO {

    private Long adminUserId;

    private Long orgId;

    private String email;

    @NotEmpty
    private String phone;

    @NotEmpty
    private String phoneCaptcha;

    @NotEmpty
    private String nationalCode;

    @NotEmpty
    private String verifyCode;
}
