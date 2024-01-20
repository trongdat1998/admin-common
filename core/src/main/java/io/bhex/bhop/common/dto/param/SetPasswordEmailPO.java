package io.bhex.bhop.common.dto.param;

import lombok.Data;

/**
 * @Description:
 * @Date: 2018/10/6 下午4:06
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Data
public class SetPasswordEmailPO {

    private Long orgId;

    private String adminWebUrl;

    private String sign;
}
