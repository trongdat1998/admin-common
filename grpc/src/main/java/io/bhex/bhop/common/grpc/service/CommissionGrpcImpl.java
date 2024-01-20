package io.bhex.bhop.common.grpc.service;

import io.bhex.base.admin.common.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.bhop.common.entity.ExchangeCommissionDetailEntity;
import io.bhex.bhop.common.entity.ExchangeCommissionEntity;
import io.bhex.bhop.common.service.CommissionService;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Date: 2018/10/13 上午11:46
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@GrpcService
public class CommissionGrpcImpl extends CommissionServiceGrpc.CommissionServiceImplBase {

    @Autowired
    private CommissionService commissionService;

    @Override
    public void saveExchangeCommission(ExchangeCommission request, StreamObserver<CommissionReply> responseObserver) {

        ExchangeCommissionEntity entity = new ExchangeCommissionEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setExchangeFee(new BigDecimal(request.getExchangeFee()));
        entity.setExchangeSaasFee(new BigDecimal(request.getExchangeSaasFee()));
        entity.setExchangeSaasFeeRate(new BigDecimal(request.getExchangeSassFeeRate()));
        entity.setTradingAmount(new BigDecimal(request.getTradingAmount()));
        entity.setTotalFee(new BigDecimal(request.getTotalFee()));
        entity.setSysFee(new BigDecimal(request.getSysFee()));
        commissionService.addExCommission(entity);

        CommissionReply reply = CommissionReply.newBuilder().setResult(true).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void saveExchangeCommissionDetail(ExchangeCommissionDetail request, StreamObserver<CommissionReply> responseObserver) {
        ExchangeCommissionDetailEntity entity = new ExchangeCommissionDetailEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setExchangeFee(new BigDecimal(request.getExchangeFee()));
        entity.setExchangeSaasFee(new BigDecimal(request.getExchangeSaasFee()));
        entity.setBrokerFee(new BigDecimal(request.getBrokerFee()));
        entity.setTradingAmount(new BigDecimal(request.getTradingAmount()));
        entity.setTotalFee(new BigDecimal(request.getTotalFee()));
        entity.setSysFee(new BigDecimal(request.getSysFee()));
        commissionService.addExCommissionDetail(entity);

        CommissionReply reply = CommissionReply.newBuilder().setResult(true).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void countExchangeCommission(CountExchangeCommissionRequest request, StreamObserver<CountReply> responseObserver) {
        int count = commissionService.countExCommission(request.getExchangeId(), request.getFeeTokenId(), request.getClearDay());
        CountReply reply = CountReply.newBuilder().setCount(count).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void countExchangeCommissionDetail(CountExchangeCommissionDetailRequest request, StreamObserver<CountReply> responseObserver) {
        int count = commissionService.countExCommissionDetail(request.getExchangeId(), request.getBrokerId(), request.getFeeTokenId(), request.getClearDay());
        CountReply reply = CountReply.newBuilder().setCount(count).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void listExchangeCommissions(ListExchangeCommissionsRequest request, StreamObserver<ListExchangeCommissionsReply> responseObserver) {
        List<ExchangeCommissionEntity> list = commissionService.listExCommissions(request.getFromTime(), request.getEndTime(), request.getExchangeName(),
                request.getBaseId(), request.getNext(), request.getLimit());
        List<ExchangeCommission> result = list.stream().map(detail->{
            ExchangeCommission.Builder builder = ExchangeCommission.newBuilder();
            BeanUtils.copyProperties(detail, builder);

            builder.setExchangeFee(detail.getExchangeFee().toPlainString());
            builder.setExchangeSaasFee(detail.getExchangeSaasFee().toPlainString());
            builder.setTradingAmount(detail.getTradingAmount().toPlainString());
            builder.setTotalFee(detail.getTotalFee().toPlainString());
            builder.setSysFee(detail.getSysFee().toPlainString());
            builder.setExchangeSassFeeRate(detail.getExchangeSaasFeeRate().toPlainString());

            return builder.build();
        }).collect(Collectors.toList());
        ListExchangeCommissionsReply reply = ListExchangeCommissionsReply.newBuilder()
                .addAllExchangeCommission(result).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void listExchangeCommissionDetails(ListExchangeCommissionDetailsRequest request,
                                              StreamObserver<ListExchangeCommissionDetailsReply> responseObserver) {
        List<ExchangeCommissionDetailEntity> list = commissionService.listExCommissionDetails(request.getExchangeId(),
                request.getExchangeCommissionId());
        List<ExchangeCommissionDetail> result = list.stream().map(detail->{
            ExchangeCommissionDetail.Builder builder = ExchangeCommissionDetail.newBuilder();
            BeanUtils.copyProperties(detail, builder);
            builder.setExchangeFee(detail.getExchangeFee().toPlainString());
            builder.setExchangeSaasFee(detail.getExchangeSaasFee().toPlainString());
            builder.setTradingAmount(detail.getTradingAmount().toPlainString());
            builder.setTotalFee(detail.getTotalFee().toPlainString());
            builder.setSysFee(detail.getSysFee().toPlainString());
            builder.setBrokerFee(detail.getBrokerFee().toPlainString());
            return builder.build();
        }).collect(Collectors.toList());
        ListExchangeCommissionDetailsReply reply = ListExchangeCommissionDetailsReply.newBuilder()
                .addAllExchangeCommissionDetail(result).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();

    }
}
