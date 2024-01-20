package io.bhex.bhop.common.entity;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.entity
 * @Author: ming.xu
 * @CreateDate: 06/10/2018 3:46 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_admin_role")
public class AdminRole {

    public final static Integer ENABLE_STATUS = 1;

    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;
    private Long orgId;
    private String name;
    private Integer enable;
    private Integer status;
    private Long createdAt;
}
