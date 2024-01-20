package io.bhex.bhop.common.controller;

import io.bhex.bhop.common.bizlog.BussinessLogAnnotation;
import io.bhex.bhop.common.dto.IpWhitelistDTO;
import io.bhex.bhop.common.dto.param.AddIpWhitelistPO;
import io.bhex.bhop.common.dto.param.DeleteIpWhitelistPO;
import io.bhex.bhop.common.dto.param.ShowIpWhitelistPO;
import io.bhex.bhop.common.service.UserIpWhitelistService;
import io.bhex.bhop.common.util.RequestUtil;
import io.bhex.bhop.common.util.ResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.controller
 * @Author: ming.xu
 * @CreateDate: 2019/3/20 4:26 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@RestController
@RequestMapping("/api/v1/ip_whitelist")
public class UserIpWhitelistController extends BaseController {

    @Autowired
    private UserIpWhitelistService userIpWhitelistService;

    @BussinessLogAnnotation
    @RequestMapping("/add")
    public ResultModel addIpWhitelist(@RequestBody AddIpWhitelistPO param) {
        param.setOrgId(getOrgId());
        param.setAdminId(getRequestUserId());
        Boolean isOk = userIpWhitelistService.addIpWhitelist(param);
        return ResultModel.ok(isOk);
    }

    @BussinessLogAnnotation
    @RequestMapping("/delete")
    public ResultModel deleteIpWhitelist(@RequestBody DeleteIpWhitelistPO param) {
        param.setOrgId(getOrgId());
        param.setAdminId(getRequestUserId());
        Boolean isOk = userIpWhitelistService.deleteIpWhitelist(param);
        return ResultModel.ok(isOk);
    }

    @BussinessLogAnnotation
    @RequestMapping("/list")
    public ResultModel showIpWhitelist(@RequestBody ShowIpWhitelistPO param) {
        param.setOrgId(getOrgId());
        param.setAdminId(getRequestUserId());
        List<IpWhitelistDTO> dtoList = userIpWhitelistService.showIpWhitelist(param);
        return ResultModel.ok(dtoList);
    }

    @BussinessLogAnnotation
    @RequestMapping("/my_ip")
    public ResultModel showMyIpInfo(HttpServletRequest request) {
        String realIP = RequestUtil.getRealIP(request);
        Map<String, String> result = new HashMap();
        result.put("ip", realIP);
        return ResultModel.ok(result);
    }
}
