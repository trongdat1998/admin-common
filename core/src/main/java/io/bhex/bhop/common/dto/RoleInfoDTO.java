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
public class RoleInfoDTO {
    
    Long roleId;
    Long orgId;
    Long userCount;
    String name;
    Boolean enable;
    Integer status;
    Long createdAt;
    List<Long> authPathIds;
}
