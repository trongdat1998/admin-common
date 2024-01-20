package io.bhex.bhop.common.grpc.service;

import io.bhex.base.admin.common.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.bhop.common.service.BusinessLogService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description:
 * @Date: 2018/12/19 下午3:24
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@GrpcService
public class BusinessLogGrpcImpl extends BusinessLogServiceGrpc.BusinessLogServiceImplBase {

    @Autowired
    private BusinessLogService businessLogService;

    @Override
    public void saveLog(SaveLogRequest request, StreamObserver<SaveLogReply> responseObserver) {
        io.bhex.bhop.common.entity.BusinessLog businessLog = new io.bhex.bhop.common.entity.BusinessLog();
        BeanUtils.copyProperties(request.getBusinessLog(), businessLog);
        businessLog.setVisible(request.getBusinessLog().getVisible() ? 1 : 0);
        businessLog.setCreated(new Timestamp(System.currentTimeMillis()));
        businessLogService.saveLog(businessLog);
        responseObserver.onNext(SaveLogReply.newBuilder().setCode(1).build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryLogs(QueryLogsRequest request, StreamObserver<QueryLogsReply> responseObserver) {
        List<BusinessLog> logs = businessLogService.queryLogs(request);
        responseObserver.onNext(QueryLogsReply.newBuilder().addAllBusinessLog(logs).build());
        responseObserver.onCompleted();

    }
}
