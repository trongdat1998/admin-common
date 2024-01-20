package io.bhex.bhop.common.dto.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @ProjectName: broker-admin
 * @Package: io.bhex.broker.admin.controller.param
 * @Author: ming.xu
 * @CreateDate: 20/08/2018 2:50 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class QueryLogsPO {

    private Long orgId;

    private String username;

    private Long startTime;

    private Long endTime;

    private String opType;

    private Long lastId = 0L;

    @NotNull
    private Integer pageSize;

    private Long userId;

}
