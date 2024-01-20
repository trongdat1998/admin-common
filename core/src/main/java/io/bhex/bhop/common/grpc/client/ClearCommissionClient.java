package io.bhex.bhop.common.grpc.client;

import io.bhex.base.clear.ClearHistoryResponse;
import io.bhex.base.clear.CommissionResponse;
import io.bhex.bhop.common.dto.TradingCommissionDTO;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @Date: 2018/10/11 下午3:28
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

public interface ClearCommissionClient {

    default Long getYesterdayClearTime(){
        Date yesterday = Date.from(LocalDate.now().plusDays(-1).atTime(12,0)
                .atZone(ZoneId.systemDefault()).toInstant());

         return yesterday.getTime();
        //return 1536132188_000L;
    }

    default String getYesterdayClearDay(){
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now().plusDays(-1));
    }



    List<CommissionResponse.TradingCommission> getCommissions(Long clearTimeInMs);

    List<CommissionResponse.TradingCommission> getExchangeCommissions(Long exchangeId, Long clearTimeInMs);

    List<CommissionResponse.TradingCommission> getBrokerCommissions(Long brokerId, Long clearTimeInMs);

    List<ClearHistoryResponse.ClearHistory> getClearHistory(Long clearTimeInMs);

    public TradingCommissionDTO convert(CommissionResponse.TradingCommission r);

}
