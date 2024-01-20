package io.bhex.bhop.common.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Table(name="tb_exchange_commission_detail")
public class ExchangeCommissionDetailEntity {
    @Id
    private Long id;

    private Long exchangeId;

    private String exchangeName;

    private Long brokerId;

    private String brokerName;

    private String feeTokenId;

    private Long clearTime;
    /**
     * 交易总额
     */
    private BigDecimal tradingAmount;

    private BigDecimal totalFee;

    private BigDecimal sysFee;

    private BigDecimal exchangeFee;

    private BigDecimal exchangeSaasFee;

    private BigDecimal brokerFee;


    /**
     * 转帐sn,取批次内最大的id
     */
    private Long sn;

    private String clearDay;

    public static ExchangeCommissionDetailEntity defaultInstance(long exchangeId, long brokerId, String feeTokenId){
        ExchangeCommissionDetailEntity commission = new ExchangeCommissionDetailEntity();
        commission.setExchangeId(exchangeId);
        commission.setBrokerId(brokerId);
        commission.setFeeTokenId(feeTokenId);
        commission.setTradingAmount(new BigDecimal("0"));
        commission.setTotalFee(new BigDecimal("0"));
        commission.setSysFee(new BigDecimal("0"));
        commission.setExchangeFee(new BigDecimal("0"));
        commission.setExchangeSaasFee(new BigDecimal("0"));
        commission.setBrokerFee(new BigDecimal("0"));
        return commission;
    }

}
