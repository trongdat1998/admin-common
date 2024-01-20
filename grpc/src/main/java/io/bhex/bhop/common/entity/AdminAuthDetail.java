package io.bhex.bhop.common.entity;

import lombok.Data;

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
@Table(name = "tb_admin_auth_detail")
public class AdminAuthDetail {

    @Id
    private Long id;
    private Long authId;
    private String name;
    private String locale;
    private Integer status;
    private Long createdAt;

}
