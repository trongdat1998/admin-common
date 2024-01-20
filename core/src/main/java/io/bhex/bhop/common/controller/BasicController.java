package io.bhex.bhop.common.controller;

import com.google.gson.Gson;
import io.bhex.bhop.common.dto.GeetestV3RegisterDTO;
import io.bhex.bhop.common.dto.param.GeetestV3RegisterPO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.service.ReCaptchaService;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.broker.common.api.client.geetest.v3.sdk.GeetestLibResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/v1/basic")
public class BasicController {

    @Resource
    private ReCaptchaService reCaptchaService;

    @AccessAnnotation(verifyLogin = false, verifyGaOrPhone = false, verifyAuth = false)
    @RequestMapping(value = "/geev3/register")
    public ResultModel<GeetestV3RegisterDTO> registerGeeV3(@RequestBody GeetestV3RegisterPO registerDto) {
        GeetestLibResult result = reCaptchaService.registerGeeV3(registerDto.getCaptchaId());
        if (result == null) {
            throw new BizException("No gee v3!");
        }
        GeetestV3RegisterDTO registerDTO = new Gson().fromJson(result.getData(), GeetestV3RegisterDTO.class);
        return ResultModel.ok(registerDTO);
    }

}
