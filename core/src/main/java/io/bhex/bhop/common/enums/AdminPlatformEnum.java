package io.bhex.bhop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description: serverName对应application.yml中的 spring.application.name
 * @Date: 2018/10/5 下午12:26
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@AllArgsConstructor
public enum AdminPlatformEnum {
    SAAS_ADMIN_PLATFROM("saas-admin-server", 1),
    EXCHANGE_ADMIN_PLATFROM("exchange-admin-server", 2),
    BROKER_ADMIN_PLATFROM("broker-admin-server", 3);


    @Setter
    @Getter
    private String severName;

    @Setter
    @Getter
    private int value;


    public static AdminPlatformEnum getByServerName(String serverName) {
        for (AdminPlatformEnum platformEnum : AdminPlatformEnum.values()) {
            if (platformEnum.getSeverName().equals(serverName)) {
                return platformEnum;
            }
        }
        return null;
    }


}
