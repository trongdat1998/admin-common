package io.bhex.bhop.common.controller;


import io.bhex.bhop.common.config.LocaleMessageService;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.bhop.common.util.filter.XssFilterException;
import io.bhex.broker.common.exception.BrokerException;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 对restcontroller抛出的异常进行处理
 * @Date: 2018/8/31 下午2:51
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

//    @Resource
//    private MessageSource messageSource;

    private static final String HEADER_ERROR_CODE = "error_code";

    @Autowired
    private LocaleMessageService localeMessageService;

    @Autowired
    private AdminLoginUserService adminLoginUserService;

    public String getLocalMsg(String key) {
        String localeMsg = localeMessageService.getMessage(key);
        return StringUtils.isEmpty(localeMsg) ? key : localeMsg;
    }

    @ExceptionHandler({ServletRequestBindingException.class,
            HttpRequestMethodNotSupportedException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            TypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public ResultModel handleIllegalRequestException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        log.warn("domain:{} requesturi:{}", request.getServerName(), request.getRequestURL(), e);
        //log.warn("validationException ", e);
        return ResultModel.validateFail(getLocalMsg("request.parameter.error"));
    }


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResultModel exception(Exception e, HttpServletRequest request) {
        log.error("domain:{} requesturi:{} qs:{}", request.getServerName(), request.getRequestURI(), request.getQueryString(), e);
        return ResultModel.validateFail(getLocalMsg("internal.error"));
    }

    @ExceptionHandler
    public ResultModel statusRuntimeException(StatusRuntimeException e, HttpServletRequest request) {
        String message = String.format("code=%s, desc=%s, keys=%s",
                e.getStatus().getCode(),
                e.getStatus().getDescription(),
                e.getTrailers() != null ? e.getTrailers().keys() : "trailers is null");
        log.error("error rpc call. domain:{} requesturi:{} qs:{} message:{}", request.getServerName(), request.getRequestURI(),
                request.getQueryString(), message, e);
        return ResultModel.error(ErrorCode.RPC_CALL_ERROR.getCode(), "internal.error");
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    public ResultModel validationException(ValidationException e, HttpServletRequest request) {
        log.warn("validationException domain:{} requesturi:{}", request.getServerName(), request.getRequestURI(), e);
        return ResultModel.validateFail(getLocalMsg("request.parameter.error"));
    }

    @ExceptionHandler(BizException.class)
    @ResponseBody
    public ResultModel bizException(BizException e, HttpServletRequest request, HttpServletResponse response) {
        if (e.getCode() == ErrorCode.LOGIN_TOKEN_ERROR.getCode()) {
            log.info("domain:{} requesturi:{} qs:{} code:{} msg:{}", request.getServerName(), request.getRequestURI(),
                    request.getQueryString(), e.getCode(), "need.login", e);
        } else {
            log.warn("BizException domain:{} requesturi:{} qs:{} code:{} msg:{}", request.getServerName(), request.getRequestURI(),
                    request.getQueryString(), e.getCode(), e.getMessage(), e);
        }

        if (e.getCode() == ErrorCode.REPEATED_LOGIN_KICKED_OUT.getCode()) {
            adminLoginUserService.loginOut(response, 0L);
        }
        return ResultModel.error(e.getCode(), getLocalMsg(e.getMessage()));
    }

    @ExceptionHandler(XssFilterException.class)
    @ResponseBody
    public ResultModel xssFilterException(XssFilterException e, HttpServletRequest request, HttpServletResponse response) {
        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", e.getField());
        String errorMessage = localeMessageService.getMessage("bhop.validation.constraints.xss.content");
        error.put("message", errorMessage);
        errors.add(error);
        ResultModel resultModel = ResultModel.error(ErrorCode.ERR_REQUEST_PARAMETER.getCode(),
                String.join(";", ""));
        resultModel.setData(errors);
        return resultModel;
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResultModel methodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrorList = result.getFieldErrors();
        List<Map<String, String>> errors = new ArrayList<>();
        List<String> messageStrings = new ArrayList<>();
        //for(FieldError fieldError: fieldErrorList){ //只显示一条错误
        FieldError fieldError = fieldErrorList.get(0);
        Map<String, String> error = new HashMap<>();
        log.info("domain:{} requesturi:{} {} {}", request.getServerName(), request.getRequestURI(),
                fieldError.getField(), fieldError.getDefaultMessage());
        String errorMsg = fieldError.getDefaultMessage();

        messageStrings.add(errorMsg);
        error.put("field", fieldError.getField());
        String errorMessage = localeMessageService.getMessage(errorMsg);
        error.put("message", !StringUtils.isEmpty(errorMessage) ? errorMessage : errorMsg);
        errors.add(error);
        //}

        ResultModel resultModel = ResultModel.error(ErrorCode.ERR_REQUEST_PARAMETER.getCode(), String.join(";", ""));
        resultModel.setData(errors);
        return resultModel;
    }


//    @ExceptionHandler({HttpMessageNotReadableException.class})
//    @ResponseBody
//    public ResultModel parameterException(HttpMessageNotReadableException e) {
//
//        return ResultModel.error(e.getMessage());
//    }

//    @ExceptionHandler({MissingServletRequestParameterException.class})
//    @ResponseBody
//    public ResultModel parameterExceptionx(MissingServletRequestParameterException e) {
//        return ResultModel.errorParameter("request parameter required:" + e.getParameterName() +"(" + e.getParameterType()+")");
//    }
//
//    @ExceptionHandler({TypeMismatchException.class})
//    @ResponseBody
//    public ResultModel mismatchParameterExceptionx(TypeMismatchException e) {
//        return ResultModel.errorParameter("request parameter:" + e.getPropertyName() + " required:" + e.getRequiredType());
//    }

    /**
     * 目前只用到滑块验证这一处会报broker exception
     * @param e
     * @param request
     * @param response
     * @return
     */
    @ExceptionHandler(BrokerException.class)
    public ResultModel handleBrokerException(BrokerException e,
                                        HttpServletRequest request, HttpServletResponse response) {
        Integer code = e.getCode();
        String message = localeMessageService.getMessage(String.valueOf(code));
        log.warn("BizException domain:{} requesturi:{} message:{}", request.getServerName(),
                request.getRequestURI(), message, e);
        return ResultModel.error(ErrorCode.RECAPTCHA_ERROR.getCode(), message);
    }
}
