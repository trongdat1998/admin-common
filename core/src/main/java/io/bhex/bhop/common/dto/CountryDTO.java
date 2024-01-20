package io.bhex.bhop.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.dto
 * @Author: ming.xu
 * @CreateDate: 2019/4/10 4:58 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
@Builder
public class CountryDTO {

    private Long id;

    private String nationalCode;

    private String shortName;

    private String countryName;

    private String countryShortName;

    private String indexName;

    private transient Integer customOrder;
}
