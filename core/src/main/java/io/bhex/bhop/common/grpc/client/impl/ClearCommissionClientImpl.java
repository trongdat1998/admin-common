package io.bhex.bhop.common.grpc.client.impl;

import io.bhex.base.clear.ClearHistoryResponse;
import io.bhex.base.clear.CommissionResponse;
import io.bhex.base.clear.CommissionServiceGrpc;
import io.bhex.base.clear.SaasRequest;
import io.bhex.bhop.common.config.GrpcConfig;
import io.bhex.bhop.common.dto.TradingCommissionDTO;
import io.bhex.bhop.common.grpc.client.ClearCommissionClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * @Description:
 * @Date: 2018/10/11 下午3:31
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Service
public class ClearCommissionClientImpl implements ClearCommissionClient {

    @Resource
    GrpcConfig grpcConfig;

    private List<CommissionResponse.TradingCommission> getCommissions(Long exchangeId, Long brokerId, Long clearTimeInMs){
        CommissionServiceGrpc.CommissionServiceBlockingStub stub = grpcConfig.clearCommissionServiceBlockingStub(GrpcConfig.SAAS_ADMIN_GRPC_CHANNEL_NAME);
        SaasRequest.Builder builder = SaasRequest.newBuilder()
                .setClearTimeMs(clearTimeInMs);
        if(exchangeId != null && exchangeId > 0){
            builder.setExchangeId(exchangeId);
        }
        if(brokerId != null && brokerId > 0){
            builder.setBrokerId(brokerId);
        }
        CommissionResponse response = stub.getCommisson(builder.build());
        return response.getTradingCommissionList();
    }
    @Override
    public List<CommissionResponse.TradingCommission> getCommissions(Long clearTimeInMs) {
        //可以为null,broker独立部署该接口不能正常调用
        return getCommissions(null, null, clearTimeInMs);
    }



    @Override
    public List<CommissionResponse.TradingCommission> getExchangeCommissions(Long exchangeId, Long clearTimeInMs) {
        //可以为null,broker独立部署该接口不能正常调用
        return getCommissions(exchangeId, null, clearTimeInMs);
    }

    @Override
    public List<CommissionResponse.TradingCommission> getBrokerCommissions(Long brokerId, Long clearTimeInMs) {
        return getCommissions(null, brokerId, clearTimeInMs);
    }

    @Override
    public List<ClearHistoryResponse.ClearHistory> getClearHistory(Long clearTimeInMs) {
        //可以为null,broker独立部署该接口不能正常调用
        CommissionServiceGrpc.CommissionServiceBlockingStub stub = grpcConfig.clearCommissionServiceBlockingStub(GrpcConfig.SAAS_ADMIN_GRPC_CHANNEL_NAME);
        SaasRequest request = SaasRequest.newBuilder().setClearTimeMs(clearTimeInMs).build();
        ClearHistoryResponse response = stub.getClearHistory(request);
        return response.getClearHistoryList();
    }

    @Override
    public TradingCommissionDTO convert(CommissionResponse.TradingCommission r){
        TradingCommissionDTO commission = new TradingCommissionDTO();
        BeanUtils.copyProperties(r, commission);
        commission.setExchangeId(r.getExchangeId());
        commission.setFeeTokenId(r.getFeeTokenId());

        commission.setExchangeSaasFee(new BigDecimal(r.getExchangeSaasFee()));
        commission.setTradingAmount(new BigDecimal(r.getTradingAmount()));
        commission.setMatchTime(new Timestamp(r.getMatchTime()));
        commission.setTotalFee(new BigDecimal(r.getTotalFee()));
        commission.setSysFee(new BigDecimal(r.getSysFee()));
        commission.setExchangeFee(new BigDecimal(r.getExchangeFee()));
        commission.setBrokerSaasFee(new BigDecimal(r.getBrokerSaasFee()));
        commission.setMatchExchangeFee(new BigDecimal(r.getMatchExchangeFee()));
        commission.setMatchExchangeFeeRate(r.getMatchExchangeFeeRate().equals("") ? BigDecimal.ZERO : new BigDecimal(r.getMatchExchangeFeeRate()));
        commission.setSysFeeRate(r.getSysFeeRate().equals("") ? BigDecimal.ZERO : new BigDecimal(r.getSysFeeRate()));
        commission.setExchangeFeeRate(r.getExchangeFeeRate().equals("") ? BigDecimal.ZERO : new BigDecimal(r.getExchangeFeeRate()));
        commission.setExchangeSassFeeRate(r.getExchangeSaasFeeRate().equals("") ? BigDecimal.ZERO : new BigDecimal(r.getExchangeSaasFeeRate()));
        commission.setMatchExchangeSaasFee(new BigDecimal(r.getMatchExchangeSaasFee()));
        commission.setMatchExchangeSaasFeeRate(r.getMatchExchangeSaasFeeRate().equals("") ? BigDecimal.ZERO : new BigDecimal(r.getMatchExchangeSaasFeeRate()));
        commission.setBrokerFee(new BigDecimal(r.getBrokerFee()));
        commission.setBrokerSaasFee(new BigDecimal(r.getBrokerSaasFee()));
        commission.setBrokerSassFeeRate(r.getBrokerSaasFeeRate().equals("") ? BigDecimal.ZERO : new BigDecimal(r.getBrokerSaasFeeRate()));
        return commission;
    }
}
