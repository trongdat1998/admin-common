package io.bhex.bhop.common.mapper;

import io.bhex.bhop.common.entity.ExchangeCommissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper
@Component
public interface ExchangeCommissionMapper extends tk.mybatis.mapper.common.Mapper<ExchangeCommissionEntity> {


//    @SelectProvider(type=ExchangeCommissionSqlProvider.class, method = "selectExCommissions")
//    List<ExchangeCommissionEntity> selectExCommissions(@Param("fromDay") String fromDay, @Param("endDay") String endDay, @Param("exchangeName") String exchangeName);


    @SelectProvider(type=ExchangeCommissionSqlProvider.class, method = "selectExCommissions")
    List<ExchangeCommissionEntity> selectExCommissions(@Param("fromTime") Long fromTime,
                                                       @Param("endTime") Long endTime,
                                                       @Param("exchangeName") String exchangeName,
                                                        @Param("baseId") long baseId,
                                                        @Param("next") boolean next,
                                                        @Param("limit") long limit);


    @Select("select count(*) from tb_exchange_commission where exchange_id=#{exchangeId} and fee_token_id=#{feeTokenId} and clear_day=#{clearDay}")
    Integer countCommission(@Param("exchangeId") Long exchangeId, @Param("feeTokenId") String feeTokenId, @Param("clearDay") String clearDay);

}
