package io.bhex.bhop.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto
 * @Author: ming.xu
 * @CreateDate: 2019/3/19 5:50 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
@Builder
public class ChangePasswordDTO {

    private Boolean success;
}
