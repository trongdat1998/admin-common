package io.bhex.bhop.common.entity;

import lombok.Data;

import javax.persistence.Table;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.entity
 * @Author: ming.xu
 * @CreateDate: 06/10/2018 3:48 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_role_auth_index")
public class RoleAuthIndex {

    public final static Integer ENABLE_STATUS = 1;

    private Long id;
    private Long orgId;
    private Long roleId;
    private Long authId;
    private Integer editStatus; //0-只读 1-可编辑
    private Integer status;
    private Long createdAt;
}
