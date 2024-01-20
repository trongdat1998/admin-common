package io.bhex.bhop.common.util;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.bhex.bhop.common.config.LocaleMessageService;
import io.bhex.bhop.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 返回结果统一封装
 * @Date: 2018/8/9 下午9:05
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
@Slf4j
@Component
@NoArgsConstructor
@AllArgsConstructor
public class ResultModel<T> implements ApplicationContextAware {


    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext _applicationContext) throws BeansException {
        if(applicationContext == null){
            applicationContext  = _applicationContext;
            localeMessageService = applicationContext.getBean(LocaleMessageService.class);
            log.info("resultmode application set over");
        }
    }

    public static LocaleMessageService localeMessageService;

    private Integer code;

    private String msg;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    private static String getLocalMsg(String key) {
        if (StringUtils.isEmpty(key)) {
            return "";
        }
        String localeMsg = localeMessageService.getMessage(key);
        return StringUtils.isEmpty(localeMsg) ? key : localeMsg;
    }

    private static String getLocalMsg(String key, Object[] args) {
        if (StringUtils.isEmpty(key)) {
            return "";
        }
        String localeMsg = localeMessageService.getMessage(key, args);
        return StringUtils.isEmpty(localeMsg) ? key : localeMsg;
    }

    public static <T> ResultModel<T> ok() {
        ResultModel r = new ResultModel();
        r.setCode(ErrorCode.OK.getCode());
        r.setMsg(getLocalMsg("request.success"));
        return r;
    }

    public static <T> ResultModel<T> ok(Object data) {
        ResultModel r = new ResultModel();
        r.setCode(ErrorCode.OK.getCode());
        r.setMsg(getLocalMsg("request.success"));
        r.setData(data);
        return r;
    }

    public static <T> ResultModel<T> ok(String msg, Object data) {
        ResultModel r = new ResultModel();
        r.setCode(ErrorCode.OK.getCode());
        r.setData(data);
        r.setMsg(getLocalMsg(msg));
        return r;
    }

    public static <T> ResultModel<T> error(String msg) {
        ResultModel r = new ResultModel();
        r.setCode(ErrorCode.ERROR.getCode());
        r.setMsg(getLocalMsg(msg));
        return r;
    }

    /**
     * minTakerPayForwardFeeRate_lt_maxMakerBonusRate 多个key分别进行国际然后组合
     * @param message message是用 _ 进行分隔 如: minTakerPayForwardFeeRate_lt_maxMakerBonusRate
     * @param <T>
     * @return
     */
    public static <T> ResultModel<T> errorWithSplit(String message) {
        ResultModel r = new ResultModel();
        r.setCode(ErrorCode.ERROR.getCode());
        String[] messages = message.split("_");
        String error = "";
        for (String key : messages) {
            String fieldVal = localeMessageService.getMessage("field." + key);
            if (fieldVal == null || fieldVal.equals("field." + key)) {
                error += localeMessageService.getMessage(key);
            } else {
                error += fieldVal;
            }
            error += " ";
        }
        r.setMsg(error);
        return r;
    }

    public static <T> ResultModel<T> error(Integer code, String msg) {
        ResultModel r = new ResultModel();
        r.setCode(code);
        r.setMsg(getLocalMsg(msg));
        return r;
    }

    public static <T> ResultModel<T> error(Integer code, String msg, Object data) {
        ResultModel r = new ResultModel();
        r.setCode(code);
        r.setMsg(getLocalMsg(msg));
        r.setData(data);
        return r;
    }

    public static <T> ResultModel<T> errorParameter(String key, String msg) {
        ResultModel r = new ResultModel();
        r.setCode(ErrorCode.ERR_REQUEST_PARAMETER.getCode());
        r.setMsg("");
        Map<String,String> error = new HashMap<>();
        error.put(key, getLocalMsg(msg));
        List<Map<String,String>> errors = new ArrayList<>();
        errors.add(error);
        r.setData(errors);
        return r;
    }

    public static <T> ResultModel<T> errorParameter(String key, String msg, Object ...args) {
        ResultModel r = new ResultModel();



        if(args == null || args.length == 0){
            r.setCode(ErrorCode.REQUEST_PARAMETER_VALIDATE_FAIL.getCode());
            r.setMsg(getLocalMsg(msg));
        }
        else{
            r.setCode(ErrorCode.ERR_REQUEST_PARAMETER.getCode());
            Object[] objects = Arrays.stream(args).map(arg -> getLocalMsg(arg+"")).collect(Collectors.toList()).toArray();
            Map<String,String> error = new HashMap<>();
            error.put(key, getLocalMsg(msg, objects));
            List<Map<String,String>> errors = new ArrayList<>();
            errors.add(error);
            r.setData(errors);
            r.setMsg(getLocalMsg(msg, objects));
        }


        return r;
    }


    public static <T> ResultModel<T> validateFail(String msg) {
        return withArgsNoData(ErrorCode.ERROR.getCode(), msg, null);
    }

    public static <T> ResultModel<T> validateFail(String msg, Object ...args) {
        return withArgsNoData(ErrorCode.ERROR.getCode(), msg, args);
    }


    public static <T> ResultModel<T> withArgs(Integer code, String msg, T data, Object ...args) {
        ResultModel r = withArgsNoData(code, msg, args);
        r.setData(data);
        return r;
    }

    /**
     *
     * @param code
     * @param msg
     * @param args 对应国际化文件中的 {1} {2},此处也可以传国际化文件中的key值
     * @param <T>
     * @return
     */
    public static <T> ResultModel<T> withArgsNoData(Integer code, String msg, Object ...args){
        ResultModel r = new ResultModel();
        r.setCode(code);
        if(args == null || args.length == 0){
            r.setMsg(getLocalMsg(msg));
        }
        else{
            Object[] objects = Arrays.stream(args).map(arg -> getLocalMsg(arg+"")).collect(Collectors.toList()).toArray();
            r.setMsg(getLocalMsg(msg, objects));
        }

        return r;
    }

}
