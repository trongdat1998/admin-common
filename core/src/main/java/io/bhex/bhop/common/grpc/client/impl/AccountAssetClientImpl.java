package io.bhex.bhop.common.grpc.client.impl;

import io.bhex.base.account.*;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.base.token.*;
import io.bhex.bhop.common.config.GrpcConfig;
import io.bhex.bhop.common.dto.param.BalanceDetailDTO;
import io.bhex.bhop.common.grpc.client.AccountAssetClient;
import io.bhex.bhop.common.util.BaseReqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Date: 2018/11/1 下午2:51
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@Service
public class AccountAssetClientImpl implements AccountAssetClient {

    @Resource
    GrpcConfig grpcConfig;

    private List<TokenDetail> queryBrokerTokens(Long brokerId, Integer current, Integer pageSize) {
        SaasTokenServiceGrpc.SaasTokenServiceBlockingStub stub = grpcConfig.saasTokenServiceBlockingStub(GrpcConfig.SAAS_ADMIN_GRPC_CHANNEL_NAME);
        QueryBrokerTokensRequest.Builder builder = QueryBrokerTokensRequest.newBuilder()
                .setCurrent(current)
                .setPageSize(pageSize)
                .setBrokerId(brokerId);
        return stub.queryBrokerTokens(builder.build()).getTokenDetailsList();
    }

    @Override
    public List<BalanceDetailDTO> getBalances(Long brokerId, Long accountId) {
        BalanceServiceGrpc.BalanceServiceBlockingStub stub = grpcConfig.balanceServiceBlockingStub(GrpcConfig.SAAS_ADMIN_GRPC_CHANNEL_NAME);
        GetBalanceDetailRequest request = GetBalanceDetailRequest
                .newBuilder()
                .setBaseRequest(BaseReqUtil.getBaseRequest(brokerId))
                .setAccountId(accountId).build();
        BalanceDetailList detailResponse = stub.getBalanceDetail(request);

        List<BalanceDetailDTO> list = detailResponse.getBalanceDetailsList().stream().map(detail -> {
            BalanceDetailDTO dto = new BalanceDetailDTO();
            BeanUtils.copyProperties(detail, dto);
            dto.setTotal(DecimalUtil.toBigDecimal(detail.getTotal()));
            dto.setAvailable(DecimalUtil.toBigDecimal(detail.getAvailable()));
            dto.setLocked(DecimalUtil.toBigDecimal(detail.getLocked()));
            dto.setTokenFullName(detail.getToken().getTokenFullName());
            return dto;
        }).collect(Collectors.toList());

        List<TokenDetail> allTokens = queryBrokerTokens(brokerId, 1, 500);
        for (TokenDetail detail : allTokens) { //没有持有币种显示为0
            long count = list.stream().filter(dto -> dto.getTokenId().equals(detail.getTokenId())).count();
            if (count > 0) {
                continue;
            }
            BalanceDetailDTO dto = new BalanceDetailDTO();
            dto.setTokenId(detail.getTokenId());
            dto.setTotal(BigDecimal.ZERO);
            dto.setAvailable(BigDecimal.ZERO);
            dto.setLocked(BigDecimal.ZERO);
            dto.setTokenFullName(detail.getTokenFullName());
            list.add(dto);
        }
        return list;
    }


    @Override
    public BalanceDetailDTO getBalance(Long accountId, String tokenId) {
        BalanceServiceGrpc.BalanceServiceBlockingStub stub = grpcConfig.balanceServiceBlockingStub(GrpcConfig.SAAS_ADMIN_GRPC_CHANNEL_NAME);
        GetBalanceDetailRequest request = GetBalanceDetailRequest.newBuilder()
                .setAccountId(accountId).addTokenId(tokenId).build();
        return getBalanceDetailDTO(stub, request);
    }

    @Override
    public BalanceDetailDTO getBalance(Long accountId, String tokenId, Long orgId) {
        BalanceServiceGrpc.BalanceServiceBlockingStub stub = grpcConfig.balanceServiceBlockingStub(GrpcConfig.SAAS_ADMIN_GRPC_CHANNEL_NAME);
        GetBalanceDetailRequest request = GetBalanceDetailRequest.newBuilder()
                .setBaseRequest(BaseReqUtil.getBaseRequest(orgId))
                .setAccountId(accountId).addTokenId(tokenId).build();
        return getBalanceDetailDTO(stub, request);
    }

    private BalanceDetailDTO getBalanceDetailDTO(BalanceServiceGrpc.BalanceServiceBlockingStub stub, GetBalanceDetailRequest request) {
        BalanceDetailList detailResponse = stub.getBalanceDetail(request);
        List<BalanceDetail> balanceDetails = detailResponse.getBalanceDetailsList();
        if (CollectionUtils.isEmpty(balanceDetails)) {
            return null;
        }
        BalanceDetail detail = balanceDetails.get(0);
        BalanceDetailDTO dto = new BalanceDetailDTO();
        BeanUtils.copyProperties(detail, dto);
        dto.setTotal(DecimalUtil.toBigDecimal(detail.getTotal()));
        dto.setAvailable(DecimalUtil.toBigDecimal(detail.getAvailable()));
        dto.setLocked(DecimalUtil.toBigDecimal(detail.getLocked()));
        dto.setTokenFullName(detail.getToken().getTokenFullName());
        return dto;
    }

    @Override
    public List<BalanceDetailDTO> getBalances(Long accountId) {
        AccountServiceGrpc.AccountServiceBlockingStub stub = grpcConfig.accountServiceBlockingStub(GrpcConfig.SAAS_ADMIN_GRPC_CHANNEL_NAME);

        GetAccountInfoRequest request = GetAccountInfoRequest.newBuilder()
                .setAccountId(accountId)
                .build();

        GetAccountInfoReply reply = stub.getAccountInfo(request);
        return getBalances(reply.getOrgId(), accountId);
    }
}
