package io.bhex.bhop.common.util.percent;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @Description: 将前端传过来的百分数转换成百分比
 * @Date: 2018/11/9 上午10:31
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public class PercentageInputDeserialize extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt, BigDecimal intoValue) throws IOException {
        return super.deserialize(p, ctxt, intoValue.divide(new BigDecimal(100)));
    }

    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException{
        String valueAsString = jsonParser.getValueAsString();
        return new BigDecimal(valueAsString).divide(new BigDecimal(100));
    }
}
