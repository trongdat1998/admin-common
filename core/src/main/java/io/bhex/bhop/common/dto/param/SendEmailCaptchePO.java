package io.bhex.bhop.common.dto.param;

import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto.po
 * @Author: ming.xu
 * @CreateDate: 15/10/2018 5:51 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class SendEmailCaptchePO extends ReCaptchaPO {

    private String email;
}
