package io.bhex.bhop.common.config;

import feign.Feign;
import feign.Headers;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.bhex.bhop.common.dto.param.BrokerInstanceRes;
import io.bhex.bhop.common.dto.param.ExchangeInstanceRes;
import io.bhex.bhop.common.enums.AdminPlatformEnum;
import io.bhex.bhop.common.util.ResultModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @Description:
 * @Date: 2018/10/8 下午5:28
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@Component("orgInstanceConfig")
public class OrgInstanceConfigImpl implements OrgInstanceConfig {

    @Override
    public void reloadInstancesCache() {
        loadExchangeInstances();
        loadBrokerInstances();
    }

    @Override
    public ExchangeInstanceRes getExchangeInstance(Long exchangeId) {
        return exchangeInstanceMap.get(exchangeId);
    }

    @Override
    public BrokerInstanceRes getBrokerInstance(Long brokerId) {
        return brokerInstanceMap.get(brokerId);
    }

    private boolean filterDomain(String requestDomain, String adminWebDomain) {
        if (StringUtils.isEmpty(adminWebDomain)) {
            return false;
        }
        String[] domains = adminWebDomain.split(",");
        Optional<String> optional = Arrays.asList(domains).stream().filter(domain -> requestDomain.endsWith(domain)).findFirst();
        return optional.isPresent() ? true : false;
    }

    @Override
    public Long getBrokerIdByDomain(String domain) {
        if (brokerInstanceMap.isEmpty()) {
            return null;
        }
        Optional<BrokerInstanceRes> optional = brokerInstanceMap.values().stream()
                .filter(broker -> filterDomain(domain, broker.getAdminWebDomain()))
                .findFirst();
        return optional.isPresent() ? optional.get().getBrokerId() : null;
    }

    @Override
    public Long getExchangeIdByDomain(String domain) {
        if (exchangeInstanceMap.isEmpty()) {
            return null;
        }
        Optional<ExchangeInstanceRes> optional = exchangeInstanceMap.values().stream()
                .filter(exchange -> filterDomain(domain, exchange.getAdminWebDomain()))
                .findFirst();
        return optional.isPresent() ? optional.get().getExchangeId() : null;
    }

    @Override
    public List<BrokerInstanceRes> listBrokerInstances() {
        return brokerInstanceMap.values().stream().collect(Collectors.toList());
    }

    @Override
    public String getAdminWebUrlByBrokerId(Long brokerId) {
        if (brokerInstanceMap.isEmpty()) {
            return null;
        }

        Optional<BrokerInstanceRes> optional = brokerInstanceMap.values().stream()
                .filter(broker -> broker.getBrokerId().equals(brokerId))
                .findFirst();
        return optional.isPresent() ? optional.get().getAdminWebUrl() : null;
    }

    @Override
    public String getAdminWebUrlByExchangeId(Long exchangeId) {
        if (exchangeInstanceMap.isEmpty()) {
            return null;
        }
        Optional<ExchangeInstanceRes> optional = exchangeInstanceMap.values().stream()
                .filter(exchange -> exchange.getExchangeId().equals(exchangeId))
                .findFirst();
        return optional.isPresent() ? optional.get().getAdminWebUrl() : null;
    }

    private interface SaasHttpClient {

        @RequestLine("GET /api/v1/instance/exchange_list")
        @Headers({"Content-Type: application/json", "AdminRequest: true"})
        ResultModel<List<ExchangeInstanceRes>> getExchangeInstanceList();

        @RequestLine("GET /api/v1/instance/broker_list")
        @Headers({"Content-Type: application/json", "AdminRequest: true"})
        ResultModel<List<BrokerInstanceRes>> getBrokerInstanceList();
    }

    private SaasHttpClient getSaasClient() {
        String saasHost = environment.getProperty("saas-admin-server.server-host", String.class);
        String saasPort = environment.getProperty("saas-admin-server.server-port", String.class);
        String saasUrl = String.format("http://%s:%s/", saasHost, saasPort);
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(SaasHttpClient.class, saasUrl);

    }

    static Map<Long, ExchangeInstanceRes> exchangeInstanceMap = new HashMap<>();

    @Scheduled(initialDelay = 3, fixedRate = 14_000)
    private void loadExchangeInstances() {
        AdminPlatformEnum platform = getAdminPlatform();
        if (platform.equals(AdminPlatformEnum.SAAS_ADMIN_PLATFROM)) {
            return;
        }
        ResultModel<List<ExchangeInstanceRes>> result = getSaasClient().getExchangeInstanceList();
        if (result.getCode() == 0) {
            List<ExchangeInstanceRes> list = result.getData();
            list.stream().forEach(item -> exchangeInstanceMap.put(item.getExchangeId(), item));
        }
        //log.info("exchange instance info:{}", exchangeInstanceMap.keySet());
    }


    static Map<Long, BrokerInstanceRes> brokerInstanceMap = new HashMap<>();

    @Scheduled(initialDelay = 3, fixedRate = 13_000)
    private void loadBrokerInstances() {
        AdminPlatformEnum platform = getAdminPlatform();
        if (platform.equals(AdminPlatformEnum.SAAS_ADMIN_PLATFROM)) {
            return;
        }
        ResultModel<List<BrokerInstanceRes>> result = getSaasClient().getBrokerInstanceList();
        if (result.getCode() == 0) {
            List<BrokerInstanceRes> list = result.getData();
            list.stream().forEach(item -> brokerInstanceMap.put(item.getBrokerId(), item));
        }
        //log.info("broker instance info:{}", brokerInstanceMap.keySet());
    }

    @Resource
    Environment environment;
    public AdminPlatformEnum getAdminPlatform() {
        String serverName = environment.getProperty("spring.application.name", String.class);
        return AdminPlatformEnum.getByServerName(serverName);
    }

}
