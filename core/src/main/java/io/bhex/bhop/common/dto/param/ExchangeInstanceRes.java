package io.bhex.bhop.common.dto.param;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description:
 * @Date: 2018/8/17 上午11:24
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeInstanceRes {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long exchangeId;

    private String exchangeName;

    private String clusterName;

    private String instanceName;

    private String gatewayUrl;

    /** admin 自动生成的域名地址 */
    private String adminWebUrl;

    private String adminInternalApiUrl;

    private String adminWebDomain;

    private Integer forbidAccess; //禁止访问

}
