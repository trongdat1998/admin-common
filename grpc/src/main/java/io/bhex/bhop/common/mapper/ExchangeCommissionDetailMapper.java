package io.bhex.bhop.common.mapper;

import io.bhex.bhop.common.entity.ExchangeCommissionDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper
@Component
public interface ExchangeCommissionDetailMapper extends tk.mybatis.mapper.common.Mapper<ExchangeCommissionDetailEntity> {


//    @SelectProvider(type=ExchangeCommissionSqlProvider.class, method = "selectExCommissionDetails")
//    List<ExchangeCommissionDetailEntity> selectExCommissionDetails(@Param("fromDay") String fromDay, @Param("endDay") String endDay);

    @Select("select * from tb_exchange_commission_detail where exchange_id=#{exchangeId} " +
            " and fee_token_id=#{feeTokenId} and clear_day=#{clearDay}")
    List<ExchangeCommissionDetailEntity> selectExCommissionDetails(@Param("exchangeId") Long exchangeId,
                                                                   @Param("feeTokenId") String feeTokenId,
                                                                   @Param("clearDay") String clearDay);


    @Select("select count(*) from tb_exchange_commission_detail where exchange_id=#{exchangeId} " +
            "and broker_id=#{brokerId} and fee_token_id=#{feeTokenId} and clear_day=#{clearDay}")
    Integer countCommissionDetails(@Param("exchangeId") Long exchangeId,@Param("brokerId") Long brokerId,
                                   @Param("feeTokenId") String feeTokenId, @Param("clearDay") String clearDay);

}
