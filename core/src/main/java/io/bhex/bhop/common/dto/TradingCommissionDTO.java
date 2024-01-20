package io.bhex.bhop.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class TradingCommissionDTO {
    private Long id;

    /**
     * ID
     */
    private Long tradeDetailId;

    private Long exchangeId;

    private Long brokerId;

    private Long matchExchangeId;

    /**
     * account ID
     */
    private Long accountId;

    private String symbolId;

    private String feeTokenId;

    private Integer side;

    /**
     * 交易总额
     */
    private BigDecimal tradingAmount;

    private Timestamp matchTime;

    private Integer isMaker;

    private BigDecimal totalFee;

    private BigDecimal sysFee;

    private BigDecimal exchangeFee;

    private BigDecimal brokerFee;

    private BigDecimal exchangeSaasFee;

    private BigDecimal brokerSaasFee;

    private BigDecimal matchExchangeFee;

    private BigDecimal matchExchangeSaasFee;

    private BigDecimal sysFeeRate;

    private BigDecimal exchangeFeeRate;

    private BigDecimal exchangeSassFeeRate;

    private BigDecimal matchExchangeSaasFeeRate;

    private BigDecimal matchExchangeFeeRate;

    private BigDecimal brokerSassFeeRate;

    /**
     * calculate step status
     */
    private Integer status;

    /**
     * 转帐sn,取批次内最大的id
     */
    private Long sn;

    private String clearDay;

    public static TradingCommissionDTO defaultExCommissionInstance(long exchangeId, String feeTokenId, String clearDay){
        TradingCommissionDTO commission = new TradingCommissionDTO();
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

    public static TradingCommissionDTO defaultExCommissionDetailInstance(long exchangeId, long brokerId,
                                                                         String feeTokenId, String clearDay){
        TradingCommissionDTO commission = new TradingCommissionDTO();
        commission.setExchangeId(exchangeId);
        commission.setBrokerId(brokerId);
        commission.setFeeTokenId(feeTokenId);
        commission.setTradingAmount(new BigDecimal("0"));
        commission.setTotalFee(new BigDecimal("0"));
        commission.setSysFee(new BigDecimal("0"));
        commission.setExchangeFee(new BigDecimal("0"));
        commission.setExchangeSaasFee(new BigDecimal("0"));
        commission.setBrokerFee(new BigDecimal("0"));
        commission.setClearDay(clearDay);
        return commission;
    }
}
