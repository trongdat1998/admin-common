package io.bhex.bhop.common.controller;

import io.bhex.bhop.common.bizlog.ExcludeLogAnnotation;
import io.bhex.bhop.common.dto.CountryDTO;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.service.CountryService;
import io.bhex.bhop.common.util.ResultModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.controller
 * @Author: ming.xu
 * @CreateDate: 2019/4/10 5:24 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
@RestController
@ExcludeLogAnnotation
@RequestMapping("/api/v1/country")
public class CountryController {

    @Autowired
    private CountryService countryService;

    @AccessAnnotation(verifyGaOrPhone = false, verifyAuth = false)
    @RequestMapping(value = "/list")
    public ResultModel<Void> countryList() {
        List<CountryDTO> countryDTOS = countryService.queryCountries();

        return ResultModel.ok(countryDTOS);
    }
}
