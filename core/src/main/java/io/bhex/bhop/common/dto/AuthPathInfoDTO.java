package io.bhex.bhop.common.dto;

import lombok.Data;

import java.util.List;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service
 * @Author: ming.xu
 * @CreateDate: 11/12/2018 10:48 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class AuthPathInfoDTO {

    private Long authId;
    private Long level;
    private Long parentId;
    private String path;
    private String frontEndPath;
    private Long createdAt;
    private Boolean enable;
    private String name;
    private List<AuthPathInfoDTO> subAuthInfos;
}
