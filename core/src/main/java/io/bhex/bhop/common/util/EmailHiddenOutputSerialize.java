package io.bhex.bhop.common.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Strings;

import java.io.IOException;

public class EmailHiddenOutputSerialize  extends JsonSerializer<String> {
    @Override
    public void serialize(String email, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        email = Strings.nullToEmpty(email);
        jsonGenerator.writeString(email.replaceAll("(?<=\\w{2}).*?(?=\\w{2}@)", "*"));
    }

}
