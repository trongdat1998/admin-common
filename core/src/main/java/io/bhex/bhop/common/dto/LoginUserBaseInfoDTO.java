package io.bhex.bhop.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto
 * @Author: ming.xu
 * @CreateDate: 2019/4/3 3:24 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
@Builder
public class LoginUserBaseInfoDTO {

    private Long orgId;

    private String username;

    private String orgName;

    private Boolean bindGA;

    private Boolean bindPhone;

    private Boolean needBind;

    private String phone;

    private String email;
}
