package io.bhex.bhop.common.dto;

import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto
 * @Author: ming.xu
 * @CreateDate: 2019/3/21 11:12 AM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
public class IpWhitelistDTO {

    public final static Integer STATUS_DELETE = 0;
    public final static Integer STATUS_PASS = 1;

    private Long id;

    private Long orgId;

    private Long adminId;

    private String adminName;

    private String ipAddress;

    private Integer status;

    private Long created;
}
