package io.bhex.bhop.common.dto.param;

import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.param
 * @Author: ming.xu
 * @CreateDate: 21/11/2018 9:17 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class ReCaptchaPO {

    private String captchaId;
    /**
     * challenge 极验v3时包含该字段，注册访问的唯一标识码
     */
    private String challenge;
    private String captchaResponse;
}
