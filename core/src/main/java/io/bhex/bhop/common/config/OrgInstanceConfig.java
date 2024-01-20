package io.bhex.bhop.common.config;

import io.bhex.bhop.common.dto.param.BrokerInstanceRes;
import io.bhex.bhop.common.dto.param.ExchangeInstanceRes;
import java.util.*;


/**
 * @Description:
 * @Date: 2018/10/8 下午5:28
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public interface OrgInstanceConfig {

    void reloadInstancesCache();

    ExchangeInstanceRes getExchangeInstance(Long exchangeId);

    BrokerInstanceRes getBrokerInstance(Long brokerId);

    Long getBrokerIdByDomain(String domain);

    Long getExchangeIdByDomain(String domain);

    List<BrokerInstanceRes> listBrokerInstances();

    String getAdminWebUrlByBrokerId(Long brokerId);

    String getAdminWebUrlByExchangeId(Long exchangeId);

}
