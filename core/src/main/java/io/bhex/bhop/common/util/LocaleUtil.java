package io.bhex.bhop.common.util;

import io.bhex.bhop.common.config.LocaleConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.util
 * @Author: ming.xu
 * @CreateDate: 21/09/2018 3:30 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public class LocaleUtil {

    private final static String SEP = "_";

    public static String getLanguage(Locale locale) {
        return locale.getLanguage() + (StringUtils.isNotEmpty(locale.getCountry())? SEP + locale.getCountry(): "");
    }

    public static String getLanguage(){
        Locale locale = LocaleContextHolder.getLocale();
        return getLanguage(locale != null ? locale : LocaleConfig.DEFAULT_LOCAL);
    }
}
