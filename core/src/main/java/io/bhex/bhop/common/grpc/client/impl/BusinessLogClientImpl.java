package io.bhex.bhop.common.grpc.client.impl;

import io.bhex.base.admin.common.*;
import io.bhex.bhop.common.config.GrpcConfig;
import io.bhex.bhop.common.grpc.client.BusinessLogClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 * @Date: 2018/12/19 下午3:38
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@Service
public class BusinessLogClientImpl implements BusinessLogClient {

    @Resource
    GrpcConfig grpcConfig;

    @Override
    public SaveLogReply saveLog(SaveLogRequest request) {
        return grpcConfig.businessLogServiceBlockingStub(GrpcConfig.ADMIN_COMMON_GRPC_CHANNEL_NAME).saveLog(request);
    }

    @Override
    public List<BusinessLog> queryLogs(QueryLogsRequest request) {
        return grpcConfig.businessLogServiceBlockingStub(GrpcConfig.ADMIN_COMMON_GRPC_CHANNEL_NAME)
                .queryLogs(request).getBusinessLogList();
    }
}
