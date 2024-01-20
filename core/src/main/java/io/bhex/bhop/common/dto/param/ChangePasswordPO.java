package io.bhex.bhop.common.dto.param;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.param
 * @Author: ming.xu
 * @CreateDate: 2019/3/19 3:13 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
public class ChangePasswordPO {

    private Long adminUserId;

    @NotEmpty
    private String oldPassword;

    @NotEmpty
    private String newPassword;
}
