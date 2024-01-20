package io.bhex.bhop.common.mapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * @Description:
 * @Date: 2018/10/12 下午3:41
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public class ExchangeCommissionSqlProvider {

    public String selectExCommissions(Map<String, Object> parameter){
        Long fromTime = (Long)parameter.get("fromTime");
        Long endTime = (Long)parameter.get("endTime");
        String exchangeName = (String)parameter.get("exchangeName");
        Long baseId = (Long) parameter.get("baseId");
        boolean next = (Boolean) parameter.get("next");
        return new SQL() {
            {
                SELECT("*");
                FROM("tb_exchange_commission");
                if (!StringUtils.isEmpty(exchangeName)) {
                    WHERE("exchange_name = #{exchangeName}");
                }
                if (fromTime != null && fromTime > 0) {
                    WHERE("clear_time >= #{fromTime}");
                }
                if (endTime != null && endTime > 0) {
                    WHERE("clear_time <= #{endTime}");
                }
                //order by id desc
                if (baseId != null && baseId > 0L) {
                    if(next){
                        WHERE("id < #{baseId}");
                        ORDER_BY("id desc");
                    }
                    else{
                        WHERE("id > #{baseId}");
                        ORDER_BY("id asc");
                    }
                }
                else {
                    ORDER_BY("id desc");
                }
            }
        }.toString()  + " LIMIT #{limit}";
    }

//
//    public String selectExCommissionDetails(Map<String, Object> parameter){
//        Long fromTime = (Long)parameter.get("fromTime");
//        Long endTime = (Long)parameter.get("endTime");
//
//        return new SQL() {
//            {
//                SELECT("*");
//                FROM("tb_exchange_commission_detail");
//                if (fromTime != null && fromTime > 0) {
//                    WHERE("clear_time >= #{fromTime}");
//                }
//                if (endTime != null && endTime > 0) {
//                    WHERE("clear_time <= #{endTime}");
//                }
//            }
//        }.toString();
//        //+ " LIMIT #{start},#{offset}";
//    }
}
