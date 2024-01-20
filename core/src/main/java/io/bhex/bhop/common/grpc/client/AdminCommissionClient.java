package io.bhex.bhop.common.grpc.client;

import io.bhex.base.admin.common.*;
import io.bhex.bhop.common.config.GrpcConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class AdminCommissionClient {

    @Resource
    GrpcConfig grpcConfig;

    private CommissionServiceGrpc.CommissionServiceBlockingStub getAdminStub(){
        return grpcConfig.commissionServiceBlockingStub(GrpcConfig.ADMIN_COMMON_GRPC_CHANNEL_NAME);
    }

    public CommissionReply saveExchangeCommission (ExchangeCommission exchangeCommission){
        CommissionReply reply = getAdminStub().saveExchangeCommission(exchangeCommission);
        return reply;
    }

    public CommissionReply  saveExchangeCommissionDetail (ExchangeCommissionDetail detail){
        CommissionReply reply = getAdminStub().saveExchangeCommissionDetail(detail);
        return reply;
    }

    public int countExchangeCommission(Long exchangeId, String feeTokenId, String clearDay){
        CountExchangeCommissionRequest request = CountExchangeCommissionRequest.newBuilder()
                .setExchangeId(exchangeId)
                .setFeeTokenId(feeTokenId)
                .setClearDay(clearDay)
                .build();
        return getAdminStub().countExchangeCommission(request).getCount();
    }

    public int countExchangeCommissionDetail(Long exchangeId, Long brokerId, String feeTokenId, String clearDay){
        CountExchangeCommissionDetailRequest request = CountExchangeCommissionDetailRequest.newBuilder()
                .setExchangeId(exchangeId)
                .setFeeTokenId(feeTokenId)
                .setClearDay(clearDay)
                .setBrokerId(brokerId)
                .build();
        return getAdminStub().countExchangeCommissionDetail(request).getCount();
    }


     public List<ExchangeCommission> listExchangeCommissions(Long fromTime, Long endTime, String exchangeName,
                                                             Long baseId, boolean next, Integer limit){
         ListExchangeCommissionsRequest request = ListExchangeCommissionsRequest.newBuilder()
                 .setExchangeName(StringUtils.isEmpty(exchangeName) ? "" : exchangeName)
                 .setFromTime(fromTime)
                 .setEndTime(endTime)
                 .setBaseId(baseId)
                 .setNext(next)
                 .setLimit(limit)

                 .build();
         return getAdminStub().listExchangeCommissions(request).getExchangeCommissionList();
     }

    public List<ExchangeCommissionDetail> listExchangeCommissionDetails(Long exchangeId, Long exchangeCommissionId){
        ListExchangeCommissionDetailsRequest request = ListExchangeCommissionDetailsRequest.newBuilder()
                .setExchangeId(exchangeId)
                .setExchangeCommissionId(exchangeCommissionId)
                .build();
        return getAdminStub().listExchangeCommissionDetails(request).getExchangeCommissionDetailList();
    }
}
