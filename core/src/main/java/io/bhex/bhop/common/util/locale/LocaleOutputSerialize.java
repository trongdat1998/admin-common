package io.bhex.bhop.common.util.locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @Description:将标准的locale转换成前端可看到的locale en_US -> en-us
 * @Date: 2018/11/11 下午3:46
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public class LocaleOutputSerialize  extends JsonSerializer<String> {

    @Override
    public void serialize(String localeValue, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        String sep = localeValue.indexOf("-") != -1 ? "-" : localeValue.indexOf("_") != -1 ? "_" : null;
        if (sep == null) {
            jsonGenerator.writeString(localeValue);
            return;
        }
        String[] arr = localeValue.split(sep);
        if (arr == null || arr.length < 2) {
            jsonGenerator.writeString(localeValue);
            return;
        }
        String frontEndLocale = arr[0] + "-" + arr[1].toLowerCase();
        jsonGenerator.writeString(frontEndLocale);
    }


}
