package io.bhex.bhop.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto
 * @Author: ming.xu
 * @CreateDate: 2019/3/19 4:03 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Builder
@Data
public class BeforeCreateGADTO {

    private String secretKey;

    private String authUrl;

    private String qrcode;

    private String email;
}
