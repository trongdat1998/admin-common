package io.bhex.bhop.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto
 * @Author: ming.xu
 * @CreateDate: 2019/3/29 2:42 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
@Builder
public class LoginInfoDTO {

    private Long orgId;

    private String username;

    private String orgName;

    private Boolean bindGA;

    private Boolean bindPhone;

    private String requestId;
}
