package io.bhex.bhop.common.entity;

import lombok.Data;

import javax.persistence.Table;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.entity
 * @Author: ming.xu
 * @CreateDate: 06/10/2018 3:49 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_user_role_index")
public class UserRoleIndex {

    public final static Integer ENABLE_STATUS = 1;

    private Long id;
    private Long orgId;
    private Long userId;
    private Long roleId;
    private Integer status;
    private Long createdAt;
}
