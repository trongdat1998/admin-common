package io.bhex.bhop.common.grpc.client;

import io.bhex.base.admin.common.BusinessLog;
import io.bhex.base.admin.common.QueryLogsRequest;
import io.bhex.base.admin.common.SaveLogReply;
import io.bhex.base.admin.common.SaveLogRequest;

import java.util.List;

public interface BusinessLogClient {

    SaveLogReply saveLog(SaveLogRequest request);

    List<BusinessLog> queryLogs(QueryLogsRequest request);
}
