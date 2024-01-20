package io.bhex.bhop.common.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.entity
 * @Author: ming.xu
 * @CreateDate: 2019/4/10 3:50 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_country")
public class Country {

    @Id
    private Long id;

    private String nationalCode;

    private String domainShortName;

    private Integer customOrder;

}
