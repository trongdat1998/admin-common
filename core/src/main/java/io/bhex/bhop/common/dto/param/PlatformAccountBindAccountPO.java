package io.bhex.bhop.common.dto.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Description:将brokerName下的username绑定到targetOrgId的accountType类型账户下面
 * @Date: 2018/10/8 下午7:19
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Data
public class PlatformAccountBindAccountPO {
    @NotNull
    private Long accountId;

    @NotNull
    private Integer accountType;

    @NotNull
    private String brokerName;//c端用户的brokerName

}
