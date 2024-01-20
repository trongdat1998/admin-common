package io.bhex.bhop.common.dto.param;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.bhex.bhop.common.util.validation.CommonInputValid;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.param
 * @Author: ming.xu
 * @CreateDate: 11/12/2018 10:37 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class UpdateRolePO {

    private Long orgId;
    private Long roleId;
    @CommonInputValid
    private String name;
    private List<Long> userIds;

    private List<Long> authPathIds;

    private Map<Long, Integer> authPaths = Maps.newHashMap();

}
