package io.bhex.bhop.common.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Strings;

import java.io.IOException;

public class MobileHiddenOutputSerialize extends JsonSerializer<String> {
    @Override
    public void serialize(String mobile, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        mobile = Strings.nullToEmpty(mobile);
        jsonGenerator.writeString(mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
    }

}
