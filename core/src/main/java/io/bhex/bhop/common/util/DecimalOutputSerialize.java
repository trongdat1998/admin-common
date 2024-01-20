package io.bhex.bhop.common.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @Description: json输出统一保留8位小数，去看后面多余的0，并且不使用科学计数法原样输出
 * @Date: 2018/10/22 下午8:11
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public class DecimalOutputSerialize extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal bigDecimal, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(convert(bigDecimal).toPlainString());

    }

    public static BigDecimal convert(BigDecimal num) {
        return num.setScale(18, BigDecimal.ROUND_DOWN)
                .stripTrailingZeros();
    }
}
