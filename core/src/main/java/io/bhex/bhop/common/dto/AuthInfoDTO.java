package io.bhex.bhop.common.dto;

import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto
 * @Author: ming.xu
 * @CreateDate: 11/12/2018 3:23 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class AuthInfoDTO {

    private Long authId;
    private Integer editAbleStatus;
    private String name;
    private String path;
    private String frontEndPath;
}
