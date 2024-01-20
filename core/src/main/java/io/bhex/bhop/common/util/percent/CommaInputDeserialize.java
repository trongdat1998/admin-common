package io.bhex.bhop.common.util.percent;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * 中文"，"转英文","
 * @ProjectName: exchange
 * @Package: io.bhex.bhop.common.util.percent
 * @Author: ming.xu
 * @CreateDate: 2019/12/9 2:21 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public class CommaInputDeserialize extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return jsonParser.getValueAsString().replaceAll("，", ",");
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt, String intoValue) throws IOException {
        return super.deserialize(p, ctxt, intoValue.replaceAll("，", ","));
    }

}
