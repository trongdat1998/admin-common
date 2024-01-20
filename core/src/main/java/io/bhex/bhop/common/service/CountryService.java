package io.bhex.bhop.common.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.bhex.base.admin.common.Country;
import io.bhex.base.admin.common.QueryCountryResponse;
import io.bhex.bhop.common.dto.CountryDTO;
import io.bhex.bhop.common.grpc.client.CountryClient;
import io.bhex.bhop.common.util.LocaleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service
 * @Author: ming.xu
 * @CreateDate: 2019/4/10 5:06 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class CountryService {

    private static ImmutableMap<String, List<CountryDTO>> countryMap = ImmutableMap.of();

    @Autowired
    private CountryClient countryClient;

    public List<CountryDTO> queryCountries() {
        return countryMap.getOrDefault(LocaleUtil.getLanguage(), Lists.newArrayList());
    }

    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 5000)
    public void initCountry() {
        try {
            QueryCountryResponse response = countryClient.queryCountries();
            List<Country> countryList = response.getCountryList();
            if (countryList != null && countryList.size() > 0) {
                Map<String, List<Country>> tmpCountryMap = countryList.stream().collect(Collectors.groupingBy(Country::getLanguage));
                Map<String, List<CountryDTO>> tmpMap = Maps.newHashMap();
                for (String language : tmpCountryMap.keySet()) {
                    tmpMap.put(language, tmpCountryMap.get(language).stream().map(this::getCountryDTO)
                            .sorted(Comparator.comparing(CountryDTO::getCustomOrder))
                            .collect(Collectors.toList()));
                }
                countryMap = ImmutableMap.copyOf(tmpMap);
            }
        } catch (Exception e) {
            log.error("refresh country cache data error!", e);
        }
    }


    private CountryDTO getCountryDTO(Country country) {
        return CountryDTO.builder()
                .id(country.getId())
                .nationalCode(country.getNationalCode())
                .countryName(country.getName())
                .shortName(country.getShortName())
                .indexName(country.getIndexName())
                .customOrder(country.getCustomOrder())
                .build();
    }
}
