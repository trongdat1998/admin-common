package io.bhex.bhop.common.config;

import com.google.common.collect.Lists;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.config
 * @Author: ming.xu
 * @CreateDate: 09/09/2018 5:26 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Configuration
public class LocaleConfig {

    public static Locale DEFAULT_LOCAL = Locale.US;

    @Bean
    public LocaleChangeInterceptor localeChange(){
        return new LocaleChangeInterceptor();
    }

//    //此用法与其它网站冲突，其它网站此处写的中划线admin会报错
//    @Bean
//    public LocaleResolver localeResolver(){
//        CookieLocaleResolver clr = new CookieLocaleResolver();
//        clr.setDefaultLocale(DEFAULT_LOCAL);
//
////        clr.setDefaultTimeZone(TimeZone.getTimeZone("GMT+8"));
//        clr.setCookieName("locale");
//        return clr;
//    }

    //与其它网站统一使用Accept-Language
    @Bean
    public LocaleResolver localeResolver(){
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        //localeResolver.setSupportedLocales(Lists.newArrayList(Locale.US, Locale.CHINA));
        localeResolver.setDefaultLocale(DEFAULT_LOCAL);
        return localeResolver;
    }


    @Bean
    public MessageSource messageSource(){
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding("utf-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setBasename("classpath:language/messages");
        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean validatorFactoryBean(){
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setValidationMessageSource(messageSource());
        return validatorFactoryBean;
    }
}
