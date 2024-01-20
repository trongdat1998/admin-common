package io.bhex.bhop.common.dto.param;

import io.bhex.bhop.common.util.validation.CommonInputValid;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.param
 * @Author: ming.xu
 * @CreateDate: 10/12/2018 6:26 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class AddRolePO {

    private Long orgId;
    @NotBlank
    @CommonInputValid
    private String name;
    private List<Long> userIds;
    private List<Long> authPathIds;

    private Map<Long, Integer> authPaths;
}
