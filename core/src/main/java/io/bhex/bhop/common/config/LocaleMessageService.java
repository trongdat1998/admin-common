package io.bhex.bhop.common.config;

import java.util.Locale;
import javax.annotation.Resource;

import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * @Description: 本地msg获取
 * @Date: 2018/9/27 下午3:26
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Component
@Slf4j
public class LocaleMessageService {

    @Resource
    private MessageSource messageSource;
    //key language value
    private static Table<String, String, String> LANGUAGE_TABLE = HashBasedTable.create();

    public void setCacheValue(Table<String, String, String> cacheValue) {
        LANGUAGE_TABLE = cacheValue;
    }

    /**
     * @param code ：对应messages配置的key.
     * @return
     */
    public String getMessage(String code){
        return getMessage(code,null);
    }

    /**
     *
     * @param code ：对应messages配置的key.
     * @param args : 数组参数.
     * @return
     */
    public String getMessage(String code,Object[] args){
        return getMessage(code, args,"");
    }


    /**
     *
     * @param code ：对应messages配置的key.
     * @param args : 数组参数.
     * @param defaultMessage : 没有设置key的时候的默认值.
     * @return
     */
    public String getMessage(String code,Object[] args,String defaultMessage){
        //这里使用比较方便的方法，不依赖request.
        Locale locale = LocaleContextHolder.getLocale();

        //先读数据库配置 from LANGUAGE_TABLE, 没有再从i18配置文件 ， 新版brokerAdmin走saas语言管理
        String message = LANGUAGE_TABLE.get(locale.toString(), code);
        if (message == null && locale != Locale.US) {
            message = LANGUAGE_TABLE.get(Locale.US.toString(), code);
        }
        if (message != null) {
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    message = message.replace("{" + i + "}", args[i].toString());
                }
            }
            return message;
        }
        try {
            message = messageSource.getMessage(code, args, defaultMessage, locale);
        } catch (Exception e) {
            //ignore
        }

        if(!Strings.isNullOrEmpty(message)) {
            return message;
        }
        log.warn("{} no i18n config", code);
        return code;
    }

//    public String getMessage(Locale locale, String code,Object[] args){
//        return messageSource.getMessage(code, args, null, locale);
//    }

}
