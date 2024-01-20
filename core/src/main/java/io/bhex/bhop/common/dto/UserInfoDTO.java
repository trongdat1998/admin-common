package io.bhex.bhop.common.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.bhex.bhop.common.util.EmailHiddenOutputSerialize;
import lombok.Data;

import java.util.List;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service
 * @Author: ming.xu
 * @CreateDate: 11/12/2018 10:48 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class UserInfoDTO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Long orgId;

    @JsonSerialize(using = EmailHiddenOutputSerialize.class)
    private String email;

    private String areaCode;

    private String telephone;

    private String username;
    private Integer status;
    private Long createdAt;
    private String createdIp;
    private String orgName;
    private String position;
    private List<String> roleNameList;
    private List<Long> roleIds;
    //是否拥有当前角色的权限
    private Boolean enable;
}
