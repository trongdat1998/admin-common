package io.bhex.bhop.common.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.entity
 * @Author: ming.xu
 * @CreateDate: 2019/3/19 11:01 AM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_user_ip_whitelist")
public class UserIpWhitelist {

    public final static Integer STATUS_DELETE = 0;
    public final static Integer STATUS_PASS = 1;

    @Id
    private Long id;

    private Long orgId;

    private Long adminId;

    private String ipAddress;

    private Integer status;

    private Long created;
}
