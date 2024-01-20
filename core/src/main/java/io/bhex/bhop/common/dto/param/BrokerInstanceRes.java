package io.bhex.bhop.common.dto.param;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrokerInstanceRes {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long brokerId;

    private String brokerName;


    /** C端访问的domain设置*/
    private String brokerWebDomain;

    /** admin 自动生成的域名地址 */
    private String adminWebUrl;

    private String adminInternalApiUrl;

    private String adminWebDomain;

    private Integer frontendCustomer; //券商前端自定义

    private Integer forbidAccess; //禁止访问

    private Long dueTime = 0L; //过期时间，0-代表未设置

}
