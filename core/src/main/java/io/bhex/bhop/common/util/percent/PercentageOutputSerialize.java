package io.bhex.bhop.common.util.percent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @Description: json输出百分数，统一保留8位小数，去掉后面多余的0，并且不使用科学计数法原样输出
 * @Date: 2018/10/22 下午8:11
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public class PercentageOutputSerialize extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal bigDecimal, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(convert(bigDecimal.multiply(new BigDecimal(100))).toPlainString());

    }

    public static BigDecimal convert(BigDecimal num){
        return num.setScale(8, BigDecimal.ROUND_DOWN)
                .stripTrailingZeros();
    }
}
