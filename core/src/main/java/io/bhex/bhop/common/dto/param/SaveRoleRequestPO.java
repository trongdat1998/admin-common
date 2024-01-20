package io.bhex.bhop.common.dto.param;

import lombok.Data;

import java.util.List;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.po
 * @Author: ming.xu
 * @CreateDate: 09/10/2018 5:15 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class SaveRoleRequestPO {

    private Long roleId;
    private Long orgId;
    private String name;
    private List<Long> userIds;
    private List<Long> roleIds;
}
