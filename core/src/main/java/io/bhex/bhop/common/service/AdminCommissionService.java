package io.bhex.bhop.common.service;

import io.bhex.base.admin.common.CommissionReply;
import io.bhex.base.admin.common.ExchangeCommission;
import io.bhex.base.admin.common.ExchangeCommissionDetail;
import io.bhex.base.clear.ClearHistoryResponse;
import io.bhex.base.clear.CommissionResponse;
import io.bhex.bhop.common.config.OrgInstanceConfig;
import io.bhex.bhop.common.dto.ExchangeCommissionDTO;
import io.bhex.bhop.common.dto.ExchangeCommissionDetailDTO;
import io.bhex.bhop.common.dto.TradingCommissionDTO;
import io.bhex.bhop.common.dto.param.BrokerInstanceRes;
import io.bhex.bhop.common.dto.param.ExchangeInstanceRes;
import io.bhex.bhop.common.grpc.client.AdminCommissionClient;
import io.bhex.bhop.common.grpc.client.ClearCommissionClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminCommissionService {

    @Autowired
    private ClearCommissionClient clearCommissionClient;
    @Autowired
    private AdminCommissionClient adminCommissionClient;
    @Autowired
    private OrgInstanceConfig orgInstanceConfig;

    public void loadExchangeCommissions(List<CommissionResponse.TradingCommission> list, String clearDay,List<ClearHistoryResponse.ClearHistory> clearHistories){

        Map<String, Boolean> insertedMap = new HashMap<>();


        List<TradingCommissionDTO> commissions =
                list.stream().map(r -> clearCommissionClient.convert(r)).collect(Collectors.toList());

        Map<String, ExchangeCommissionDTO> map = new HashMap<>();
        for(TradingCommissionDTO clearData : commissions){
            String key = clearData.getExchangeId() + "." + clearData.getFeeTokenId();
            Boolean inserted = insertedMap.get(key);
            log.info("key:{} inserted:{}", key, inserted);
            if(inserted == null){
                int count = adminCommissionClient.countExchangeCommission(clearData.getExchangeId(), clearData.getFeeTokenId(), clearDay);
                inserted = count > 0;
                insertedMap.put(key,inserted);
            }
            if(!inserted){
                ExchangeInstanceRes exInstance = orgInstanceConfig.getExchangeInstance(clearData.getExchangeId());

                map.putIfAbsent(key, ExchangeCommissionDTO.defaultInstance(clearData.getExchangeId(),
                        clearData.getFeeTokenId(), clearDay));

                ExchangeCommissionDTO exCommission = map.get(key);
                exCommission.setExchangeName(exInstance != null ? exInstance.getExchangeName() : "empty");
                exCommission.setSn(clearData.getSn());
                exCommission.setClearDay(clearDay);
                exCommission.setExchangeSassFeeRate(clearData.getExchangeSassFeeRate());
                exCommission.setTradingAmount(clearData.getTradingAmount().add(exCommission.getTradingAmount()));
                exCommission.setTotalFee(clearData.getTotalFee().add(exCommission.getTotalFee()));
                exCommission.setSysFee(clearData.getSysFee().add(exCommission.getSysFee()));
                exCommission.setExchangeSaasFee(clearData.getExchangeSaasFee().add(exCommission.getExchangeSaasFee()));
                exCommission.setExchangeFee(clearData.getExchangeFee().subtract(clearData.getExchangeSaasFee())
                        .subtract(clearData.getMatchExchangeFee()).add(exCommission.getExchangeFee()));
                exCommission.setClearTime(getClearTime(clearHistories, clearData.getSn()));
                map.put(key, exCommission);
            }



            //处理撮合方数据
            Long matchExchangeId = clearData.getMatchExchangeId() > 0 ? clearData.getMatchExchangeId() : clearData.getExchangeId();
            String matchKey = matchExchangeId + "." + clearData.getFeeTokenId();
            Boolean matchKeyInserted = insertedMap.get(matchKey);
            if(matchKeyInserted == null){
                int count = adminCommissionClient.countExchangeCommission(matchExchangeId, clearData.getFeeTokenId(), clearDay);
                matchKeyInserted = count > 0;
                insertedMap.put(key, matchKeyInserted);
            }
            log.info("key:{} inserted:{}", matchKey, matchKeyInserted);
            if(!matchKeyInserted){
                ExchangeInstanceRes matchExInstance = orgInstanceConfig.getExchangeInstance(clearData.getExchangeId());
                map.putIfAbsent(matchKey, ExchangeCommissionDTO.defaultInstance(clearData.getExchangeId(),
                        clearData.getFeeTokenId(), clearDay));
                ExchangeCommissionDTO matchCommission = map.get(key);
                matchCommission.setExchangeName(matchExInstance != null ? matchExInstance.getExchangeName() : "empty");
                matchCommission.setSn(clearData.getSn());
                matchCommission.setClearDay(clearDay);
                matchCommission.setExchangeSassFeeRate(clearData.getExchangeSassFeeRate());
                matchCommission.setTradingAmount(clearData.getTradingAmount().add(matchCommission.getTradingAmount()));

                matchCommission.setTotalFee(clearData.getMatchExchangeFee().subtract(clearData.getExchangeSaasFee()).add(matchCommission.getTotalFee()));
                matchCommission.setSysFee(clearData.getSysFee().add(matchCommission.getSysFee()));

                matchCommission.setExchangeSaasFee(clearData.getMatchExchangeSaasFee()
                        .add(matchCommission.getExchangeSaasFee()));
                matchCommission.setExchangeFee(clearData.getMatchExchangeFee().subtract(clearData.getMatchExchangeSaasFee()).add(matchCommission.getExchangeFee()));
                matchCommission.setClearTime(getClearTime(clearHistories, clearData.getSn()));
                map.put(matchKey, matchCommission);
            }
        }

        for(String key : map.keySet()){
            ExchangeCommission.Builder builder = ExchangeCommission.newBuilder();
            log.info("content:{}  {}", key, map.get(key));
            ExchangeCommissionDTO dto = map.get(key);
            dto.setId(0L);
            BeanUtils.copyProperties(dto, builder);

            builder.setExchangeFee(dto.getExchangeFee().toPlainString());
            builder.setExchangeSaasFee(dto.getExchangeSaasFee().toPlainString());
            builder.setExchangeSassFeeRate(dto.getExchangeSassFeeRate().toPlainString());
            builder.setSysFee(dto.getSysFee().toPlainString());
            builder.setTotalFee(dto.getTotalFee().toPlainString());
            builder.setTradingAmount(dto.getTradingAmount().toPlainString());

            CommissionReply reply = adminCommissionClient.saveExchangeCommission(builder.build());
            log.info("add YesterdayExchangeCommissions {} {}", key, reply);
        }
    }


    public void loadAllYesterdayExchangeCommissions(){
        String yesterdayClearDay = clearCommissionClient.getYesterdayClearDay();
        long yesterdayClearTime = clearCommissionClient.getYesterdayClearTime();
        List<ClearHistoryResponse.ClearHistory> clearHistories = clearCommissionClient.getClearHistory(yesterdayClearTime);

        List<CommissionResponse.TradingCommission> list = clearCommissionClient.getCommissions(yesterdayClearTime);
        loadExchangeCommissions(list, yesterdayClearDay,clearHistories);
    }

    private Long getClearTime(List<ClearHistoryResponse.ClearHistory> clearHistories, Long sn){
        if(CollectionUtils.isEmpty(clearHistories)){
            return System.currentTimeMillis();
        }
        Optional<ClearHistoryResponse.ClearHistory> optional = clearHistories.stream().filter(h->h.getSn() == sn).findFirst();
        return optional.isPresent() ? optional.get().getClearTime() : System.currentTimeMillis();
    }

    public void loadYesterdayExchangeCommissions(Long exchangeId){
        String yesterdayClearDay = clearCommissionClient.getYesterdayClearDay();
        long yesterdayClearTime = clearCommissionClient.getYesterdayClearTime();
        List<ClearHistoryResponse.ClearHistory> clearHistories = clearCommissionClient.getClearHistory(yesterdayClearTime);
        List<CommissionResponse.TradingCommission> list = clearCommissionClient.getExchangeCommissions(exchangeId, yesterdayClearTime);
        loadExchangeCommissions(list, yesterdayClearDay,clearHistories);
    }

    public void loadYesterdayExchangeCommissionDetails(Long exchangeId){
        String yesterdayClearDay = clearCommissionClient.getYesterdayClearDay();
        long yesterdayClearTime = clearCommissionClient.getYesterdayClearTime();
        List<ClearHistoryResponse.ClearHistory> clearHistories = clearCommissionClient.getClearHistory(yesterdayClearTime);
        List<CommissionResponse.TradingCommission> list = clearCommissionClient.getExchangeCommissions(exchangeId, yesterdayClearTime);
        loadExchangeCommissionDetails(list, yesterdayClearDay, clearHistories);
    }

    public void loadExchangeCommissionDetails(List<CommissionResponse.TradingCommission> list, String clearDay,List<ClearHistoryResponse.ClearHistory> clearHistories){
        List<TradingCommissionDTO> commissions =
                list.stream().map(r -> clearCommissionClient.convert(r)).collect(Collectors.toList());
        Map<String, Boolean> insertedMap = new HashMap<>();
        Map<String, ExchangeCommissionDetailDTO> detailMap = new HashMap<>();
        for(TradingCommissionDTO clearData : commissions){
            String key = clearData.getExchangeId() + "." + clearData.getBrokerId() + "." + clearData.getFeeTokenId();
            Boolean inserted = insertedMap.get(key);
            if(inserted == null){
                int count = adminCommissionClient.countExchangeCommissionDetail(clearData.getExchangeId(), clearData.getBrokerId(),
                        clearData.getFeeTokenId(), clearDay);
                inserted = count > 0;
                insertedMap.put(key, inserted);
            }
            log.info("key:{} inserted:{}", key, inserted);
            if(!inserted){
                detailMap.putIfAbsent(key, ExchangeCommissionDetailDTO.defaultInstance(clearData.getExchangeId(),
                        clearData.getBrokerId(), clearData.getFeeTokenId(), clearDay));

                ExchangeInstanceRes exInstance = orgInstanceConfig.getExchangeInstance(clearData.getExchangeId());
                BrokerInstanceRes brokerInstance = orgInstanceConfig.getBrokerInstance(clearData.getBrokerId());
                ExchangeCommissionDetailDTO exCommissionDetail = detailMap.get(key);
                exCommissionDetail.setExchangeName(exInstance != null ? exInstance.getExchangeName() : "empty");
                exCommissionDetail.setBrokerName(brokerInstance.getBrokerName());
                exCommissionDetail.setSn(clearData.getSn());
                exCommissionDetail.setTradingAmount(clearData.getTradingAmount().add(exCommissionDetail.getTradingAmount()));
                exCommissionDetail.setTotalFee(clearData.getTotalFee().add(exCommissionDetail.getTotalFee()));
                exCommissionDetail.setSysFee(clearData.getSysFee().add(exCommissionDetail.getSysFee()));
                exCommissionDetail.setExchangeSaasFee(clearData.getExchangeSaasFee().add(exCommissionDetail.getExchangeSaasFee()));
                exCommissionDetail.setExchangeFee(clearData.getExchangeFee().subtract(clearData.getExchangeSaasFee())
                        .add(exCommissionDetail.getExchangeFee()));
                exCommissionDetail.setBrokerFee(clearData.getBrokerFee().subtract(clearData.getBrokerSaasFee()).add(exCommissionDetail.getBrokerFee()));
                exCommissionDetail.setClearTime(getClearTime(clearHistories, clearData.getSn()));
                detailMap.put(key, exCommissionDetail);
            }
        }

        for(String key : detailMap.keySet()){
            ExchangeCommissionDetail.Builder builder = ExchangeCommissionDetail.newBuilder();
            ExchangeCommissionDetailDTO dto = detailMap.get(key);
            dto.setId(0L);
            BeanUtils.copyProperties(dto, builder);
            builder.setExchangeFee(dto.getExchangeFee().toPlainString());
            builder.setExchangeSaasFee(dto.getExchangeSaasFee().toPlainString());
            builder.setBrokerFee(dto.getBrokerFee().toPlainString());
            builder.setSysFee(dto.getSysFee().toPlainString());
            builder.setTotalFee(dto.getTotalFee().toPlainString());
            builder.setTradingAmount(dto.getTradingAmount().toPlainString());

            BeanUtils.copyProperties(dto, builder);
            CommissionReply reply = adminCommissionClient.saveExchangeCommissionDetail(builder.build());
            log.info("add YesterdayExchangeCommissionDetails {} {}", key, reply);
        }
    }

    public List<ExchangeCommissionDTO> getExchangeCommissions(Long fromTime, Long endTime, String exchangeName,
                                                           Long baseId, boolean next, Integer limit){
        List<ExchangeCommission> list = adminCommissionClient.listExchangeCommissions(fromTime,endTime,
                exchangeName,baseId,next,limit);
        List<ExchangeCommissionDTO> dtos = list.stream().map(e->{
            ExchangeCommissionDTO dto = new ExchangeCommissionDTO();
            BeanUtils.copyProperties(e, dto);
            dto.setExchangeFee(new BigDecimal(e.getExchangeFee()));
            dto.setExchangeSaasFee(new BigDecimal(e.getExchangeSaasFee()));
            dto.setTradingAmount(new BigDecimal(e.getTradingAmount()));
            dto.setTotalFee(new BigDecimal(e.getTotalFee()));
            dto.setSysFee(new BigDecimal(e.getSysFee()));
            dto.setExchangeSassFeeRate(new BigDecimal(e.getExchangeSassFeeRate()));


            return dto;
        }).collect(Collectors.toList());

        return dtos;
    }

    public List<ExchangeCommissionDetailDTO> getExchangeCommissionDetails(Long exchangeId, Long exchangeCommissionId){

        List<ExchangeCommissionDetail> list = adminCommissionClient.listExchangeCommissionDetails(exchangeId, exchangeCommissionId);
        List<ExchangeCommissionDetailDTO> dtos = list.stream().map(e->{
            ExchangeCommissionDetailDTO dto = new ExchangeCommissionDetailDTO();
            BeanUtils.copyProperties(e, dto);
            dto.setExchangeFee(new BigDecimal(e.getExchangeFee()));
            dto.setExchangeSaasFee(new BigDecimal(e.getExchangeSaasFee()));
            dto.setBrokerFee(new BigDecimal(e.getBrokerFee()));
            dto.setTradingAmount(new BigDecimal(e.getTradingAmount()));
            dto.setTotalFee(new BigDecimal(e.getTotalFee()));
            dto.setSysFee(new BigDecimal(e.getSysFee()));
            return dto;
        }).collect(Collectors.toList());

        return dtos;

    }


    public void loadYesterdayBrokerCommission(){

    }


}
