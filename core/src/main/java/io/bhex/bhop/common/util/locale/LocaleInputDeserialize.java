package io.bhex.bhop.common.util.locale;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Locale;

/**
 * @Description:将前端传过来的locale转换成标准的locale形式  en-us -> en_US
 * @Date: 2018/11/11 下午3:48
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public class LocaleInputDeserialize  extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt, String inputLocale) throws IOException {
//        String sep = inputLocale.indexOf("-") != -1 ? "-" :
//                inputLocale.indexOf("_") != -1 ? "_" : null;
//        if(sep == null){
//            return inputLocale;
//        }
//        String[] arr = inputLocale.split(sep);
//        if(arr == null || arr.length < 2){
//            return inputLocale;
//        }
//
//        Locale locale = new Locale(arr[0], arr[1]);
//        return super.deserialize(p, ctxt, locale.toString());
        return inputLocale;
    }

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String inputLocale = jsonParser.getValueAsString();
         String sep = inputLocale.indexOf("-") != -1 ? "-"
                 : inputLocale.indexOf("_") != -1 ? "_" : null;
        if (sep == null) {
            return inputLocale;
        }
        String[] arr = inputLocale.split(sep);
        if (arr == null || arr.length < 2) {
            return inputLocale;
        }

        Locale locale = new Locale(arr[0], arr[1]);
        return locale.toString();
    }
}
