package io.bhex.bhop.common.dto.param;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.param
 * @Author: ming.xu
 * @CreateDate: 11/12/2018 10:38 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class SaveUserRolePO {

    private Long orgId;

    @NotNull
    @Positive
    private Long userId;
    private List<Long> roleIds;
}
