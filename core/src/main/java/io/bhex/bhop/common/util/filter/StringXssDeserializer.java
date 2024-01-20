package io.bhex.bhop.common.util.filter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;

public class StringXssDeserializer  extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        String source = p.getText().trim();
        // 把字符串做XSS过滤
        return source;
    }
}
