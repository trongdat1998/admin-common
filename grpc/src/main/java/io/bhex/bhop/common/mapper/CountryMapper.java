package io.bhex.bhop.common.mapper;

import io.bhex.bhop.common.entity.Country;
import io.bhex.bhop.common.entity.CountryDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.mapper
 * @Author: ming.xu
 * @CreateDate: 2019/4/10 3:56 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Mapper
@Service
public interface CountryMapper {

    String COLUMNS = "id, national_code, domain_short_name, custom_order";
    String DETAIL_COLUMNS = "id, country_id, country_name, index_name, language";

    @Select("SELECT " + COLUMNS + " FROM tb_country WHERE `status`=1")
    List<Country> queryAllCountry();

    @Select("SELECT " + DETAIL_COLUMNS + " FROM tb_country_detail")
    List<CountryDetail> queryAllCountryDetail();

}

