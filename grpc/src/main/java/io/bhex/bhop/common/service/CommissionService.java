package io.bhex.bhop.common.service;

import io.bhex.bhop.common.entity.ExchangeCommissionDetailEntity;
import io.bhex.bhop.common.entity.ExchangeCommissionEntity;
import io.bhex.bhop.common.mapper.ExchangeCommissionDetailMapper;
import io.bhex.bhop.common.mapper.ExchangeCommissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Date: 2018/10/13 上午10:50
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Service
public class CommissionService {
    @Autowired
    private ExchangeCommissionMapper exchangeCommissionMapper;

    @Autowired
    private ExchangeCommissionDetailMapper exchangeCommissionDetailMapper;

    public void addExCommission(ExchangeCommissionEntity exchangeCommission){
        exchangeCommission.setId(null);
        exchangeCommissionMapper.insert(exchangeCommission);
    }

    public void addExCommissionDetail(ExchangeCommissionDetailEntity detail){
        detail.setId(null);
        exchangeCommissionDetailMapper.insert(detail);
    }

    public int countExCommission(Long exchangeId, String feeTokenId, String clearDay){
        return exchangeCommissionMapper.countCommission(exchangeId, feeTokenId, clearDay);
    }

    public int countExCommissionDetail(Long exchangeId, Long brokerId, String feeTokenId, String clearDay){
        return exchangeCommissionDetailMapper.countCommissionDetails(exchangeId, brokerId, feeTokenId, clearDay);
    }

    public List<ExchangeCommissionEntity> listExCommissions(Long fromTime, Long endTime, String exchangeName,
                                                            Long baseId, boolean next, Integer limit){
//        Long baseId = fromId>0 ? fromId : endId;
//        boolean next = fromId>0 ? false : true;
        List<ExchangeCommissionEntity> list = exchangeCommissionMapper.selectExCommissions(fromTime, endTime,
                exchangeName, baseId, next, limit);
        if(CollectionUtils.isEmpty(list)){
            return new ArrayList<>();
        }
        if(!next){
            Collections.reverse(list);
        }
        return list;
    }

    public List<ExchangeCommissionDetailEntity> listExCommissionDetails(Long exchangeId, Long exCommissionId){
        ExchangeCommissionEntity entity = exchangeCommissionMapper.selectByPrimaryKey(exCommissionId);
        if(entity == null){
            return new ArrayList<>();
        }
        if(entity.getExchangeId() != exchangeId){
            //return new ArrayList<>();
        }
        return exchangeCommissionDetailMapper.selectExCommissionDetails(entity.getExchangeId(), entity.getFeeTokenId(), entity.getClearDay());
    }
}
