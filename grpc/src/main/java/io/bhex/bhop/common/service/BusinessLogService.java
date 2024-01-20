package io.bhex.bhop.common.service;

import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import io.bhex.base.admin.common.QueryLogsRequest;
import io.bhex.bhop.common.entity.BusinessLog;
import io.bhex.bhop.common.mapper.BusinessLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Date: 2018/12/19 下午2:55
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@Service
public class BusinessLogService {

    @Autowired
    private BusinessLogMapper businessLogMapper;

    public void saveLog(BusinessLog log) {
        String requestInfo = log.getRequestInfo();
        if (requestInfo != null && requestInfo.length() > 5000) {
            log.setRequestInfo(requestInfo.substring(0, 5000));
        }
        log.setCreated(new Timestamp(System.currentTimeMillis()));
        businessLogMapper.insertSelective(log);
    }

    public List<io.bhex.base.admin.common.BusinessLog> queryLogs(QueryLogsRequest request) {
        Example example =  Example.builder(BusinessLog.class)
                .orderByDesc("id")
                .build();
        if (!request.getWithRequestInfo()) {
            example.excludeProperties("requestInfo");
        }
        PageHelper.startPage(0, request.getPageSize());
        Example.Criteria criteria =   example.createCriteria()
                .andEqualTo("orgId", request.getOrgId())
                .andEqualTo("visible", 1)
                .andEqualTo("resultCode", 0);
        if (StringUtils.isNotEmpty(request.getUsername())) {
            criteria.andEqualTo("username", request.getUsername());
        }
        if (request.getStartTime() > 0) {
            criteria.andGreaterThan("created", new Timestamp(request.getStartTime()));
        }
        if (request.getEndTime() > 0) {
            criteria.andLessThan("created", new Timestamp(request.getEndTime()));
        }
        if (StringUtils.isNotEmpty(request.getOpType())) {
            criteria.andEqualTo("opType", request.getOpType());
        }
        if (!CollectionUtils.isEmpty(request.getOpTypesList())) {
            criteria.andIn("opType", request.getOpTypesList());
        }
        if (request.getFromId() > 0) {
            criteria.andLessThan("id", request.getFromId());
        }
        if (StringUtils.isNotEmpty(request.getEntityId())) {
            criteria.andEqualTo("entityId", request.getEntityId());
        }
        if (!CollectionUtils.isEmpty(request.getEntityIdsList())) {
            criteria.andIn("entityId", request.getEntityIdsList());
        }
        List<BusinessLog> logs = businessLogMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(logs)) {
            return new ArrayList<>();
        }
        List<io.bhex.base.admin.common.BusinessLog> grpcLogs = logs.stream().map(l -> {
            l.setUserAgent(Strings.nullToEmpty(l.getUserAgent()));
            l.setRequestInfo(Strings.nullToEmpty(l.getRequestInfo()));
            io.bhex.base.admin.common.BusinessLog.Builder builder = io.bhex.base.admin.common.BusinessLog.newBuilder();
            BeanUtils.copyProperties(l, builder);
            builder.setCreated(l.getCreated().getTime());
            return builder.build();
        }).collect(Collectors.toList());
        return grpcLogs;


    }


}
