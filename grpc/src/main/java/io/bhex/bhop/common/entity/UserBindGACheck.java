package io.bhex.bhop.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.entity
 * @Author: ming.xu
 * @CreateDate: 2019/3/14 12:12 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_user_bind_ga_check")
public class UserBindGACheck {

    private Long id;
    private Long orgId;
    private Long userId;
    private String gaKey;
    private Long expired;
    private Long created;

}
