package io.bhex.bhop.common.service.impl;

import io.bhex.base.admin.common.QueryCountryResponse;
import io.bhex.bhop.common.entity.Country;
import io.bhex.bhop.common.entity.CountryDetail;
import io.bhex.bhop.common.mapper.CountryMapper;
import io.bhex.bhop.common.service.ICountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service.impl
 * @Author: ming.xu
 * @CreateDate: 2019/4/10 4:20 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Service
public class CountryServiceImpl implements ICountryService {

    @Autowired
    private CountryMapper countryMapper;

    @Override
    public QueryCountryResponse queryCountries() {
        List<Country> localCountryList = countryMapper.queryAllCountry();
        List<Long> countryIdList = localCountryList.stream().map(Country::getId).collect(Collectors.toList());

        Map<Long, Country> tmpCountryMap = localCountryList.stream().collect(Collectors.toMap(Country::getId, Function.identity()));
        List<CountryDetail> countryDetailList = countryMapper.queryAllCountryDetail();
        countryDetailList = countryDetailList.stream()
                .filter(detail -> countryIdList.contains(detail.getCountryId()))
                .collect(Collectors.toList());
        List<io.bhex.base.admin.common.Country> countryList = countryDetailList.stream()
                .map(item -> getCountry(tmpCountryMap.get(item.getCountryId()), item)).collect(Collectors.toList());
        return QueryCountryResponse.newBuilder().addAllCountry(countryList).build();
    }

    private io.bhex.base.admin.common.Country getCountry(Country country, CountryDetail detail) {
        return io.bhex.base.admin.common.Country.newBuilder()
                .setId(country.getId())
                .setNationalCode(country.getNationalCode())
                .setShortName(country.getDomainShortName())
                .setName(detail.getCountryName())
                .setIndexName(detail.getIndexName())
                .setLanguage(detail.getLanguage())
                .setCustomOrder(country.getCustomOrder())
                .build();
    }
}
