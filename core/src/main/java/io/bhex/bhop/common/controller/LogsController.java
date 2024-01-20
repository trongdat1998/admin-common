package io.bhex.bhop.common.controller;

import com.google.common.base.Strings;
import io.bhex.base.admin.common.BusinessLog;
import io.bhex.base.admin.common.QueryLogsRequest;
import io.bhex.bhop.common.dto.param.QueryLogsPO;
import io.bhex.bhop.common.grpc.client.BusinessLogClient;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.service.AdminUserNameService;
import io.bhex.bhop.common.util.ResultModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/logs")
public class LogsController extends BaseController {

    @Resource
    private BusinessLogClient businessLogClient;
    @Autowired
    private AdminUserNameService adminUserNameService;

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public ResultModel queryLogs(@RequestBody QueryLogsPO po) {
       return queryLogs(getOrgId(), po, false);
    }

    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/query_internal_logs", method = RequestMethod.POST)
    public ResultModel queryInternalLogs(@RequestBody QueryLogsPO po) {
        return queryLogs(po.getOrgId(), po, true);
    }


    public ResultModel queryLogs(long orgId, QueryLogsPO po, boolean withRequestInfo) {
        QueryLogsRequest request = QueryLogsRequest.newBuilder()
                .setOrgId(orgId)
                .setFromId(po.getLastId() != null ? po.getLastId() : 0)
                .setStartTime(po.getStartTime() != null ? po.getStartTime() : 0)
                .setEndTime(po.getEndTime() != null ? po.getEndTime() : 0)
                .setPageSize(po.getPageSize())
                .setOpType(StringUtils.isNotEmpty(po.getOpType()) ? po.getOpType() : "")
                .setUsername(Strings.nullToEmpty(po.getUsername()))
                .setWithRequestInfo(withRequestInfo)
                .build();
        List<BusinessLog> logs = businessLogClient.queryLogs(request);

        List<Map<String, Object>> items = logs.stream().map(l -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", l.getId());
            item.put("username", adminUserNameService.getAdminName(l.getOrgId(), l.getUsername()));
            item.put("opType", l.getOpType());
            item.put("ip", l.getIp());
            item.put("requestUrl", l.getRequestUrl());
            item.put("remark", l.getRemark());
            item.put("created", l.getCreated());
            item.put("resultCode", l.getResultCode());

            return item;
        }).collect(Collectors.toList());



        return ResultModel.ok(items);
    }
}
