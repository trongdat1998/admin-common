package io.bhex.bhop.common.entity;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.entity
 * @Author: ming.xu
 * @CreateDate: 06/10/2018 3:48 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_admin_auth")
public class AdminAuth {

    public final static Integer ENABLE_STATUS = 1;

    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;
    private Long level;
    private Long parentId;
    private String path;
    private String frontEndPath;
    private Integer status;
    private Long createdAt;

}
