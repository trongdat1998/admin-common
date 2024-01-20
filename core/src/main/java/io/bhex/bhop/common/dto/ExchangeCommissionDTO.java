package io.bhex.bhop.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.bhex.bhop.common.util.DecimalOutputSerialize;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeCommissionDTO {
    private Long id;

    private Long exchangeId;
    private String exchangeName;

    private String feeTokenId;

    private Long clearTime;
    /**
     * 交易总额
     */
    @JsonSerialize(using = DecimalOutputSerialize.class)
    private BigDecimal tradingAmount;
    @JsonSerialize(using = DecimalOutputSerialize.class)
    private BigDecimal totalFee;
    @JsonSerialize(using = DecimalOutputSerialize.class)
    private BigDecimal sysFee;
    @JsonSerialize(using = DecimalOutputSerialize.class)
    private BigDecimal exchangeFee;
    @JsonSerialize(using = DecimalOutputSerialize.class)
    private BigDecimal exchangeSaasFee;
    @JsonSerialize(using = DecimalOutputSerialize.class)
    private BigDecimal exchangeSassFeeRate;


    /**
     * 转帐sn,取批次内最大的id
     */
    @JsonIgnore
    private Long sn;
    @JsonIgnore
    private String clearDay;

    public static ExchangeCommissionDTO defaultInstance(long exchangeId, String feeTokenId, String clearDay){
        ExchangeCommissionDTO commission = new ExchangeCommissionDTO();
        commission.setExchangeId(exchangeId);
        commission.setFeeTokenId(feeTokenId);
        commission.setTradingAmount(new BigDecimal("0"));
        commission.setTotalFee(new BigDecimal("0"));
        commission.setSysFee(new BigDecimal("0"));
        commission.setExchangeFee(new BigDecimal("0"));
        commission.setExchangeSaasFee(new BigDecimal("0"));
        commission.setExchangeSassFeeRate(new BigDecimal("0"));
        commission.setClearDay(clearDay);
        return commission;
    }

}
