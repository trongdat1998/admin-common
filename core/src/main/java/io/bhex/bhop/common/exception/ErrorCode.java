package io.bhex.bhop.common.exception;

public enum
ErrorCode {

    OK(0, "request.success"),
    ERROR(1, "error"),
    ERR_REQUEST_PARAMETER(2, "request.parameter.error"),
    REQUEST_PARAMETER_VALIDATE_FAIL(7, "request parameter validate fail"),
    NOT_FOUND(3, "not.found"),
    LOGIN_TOKEN_ERROR(5, "need.login"),
    RPC_CALL_ERROR(6, "rpc error"),
    RECAPTCHA_ERROR(7, "reCaptcha verification failed"),
    ACCOUNT_IDS_ERROR(8, "account ids error"),
    ACCOUNT_IDS_EXISTED(9, "account ids error"),
    AUTH_ERROR(10, "Authentication error"),
    WRONG_PASSWORD(11, "change.password.wrong"),
    USER_IDS_ERROR(12, "userId.error"),
    REPEATED_LOGIN_KICKED_OUT(13, "repeated.login.kicked.out"),
    TRANSFER_ERROR(14,"transfer fail"),
    NO_PERMISSION(403, "Hasn't.permission"),
    EXPIRED(406, "exceed the time limit"),
    // 空投异常
    AIRDROP_WRONG_USERID_ERROR(30001, "airdrop.wrong.userid.error"),
    EXCHANGE_RATE_ERROR(30002, "exchange.rate.error"),
    AIRDROP_WRONG_RATE_ERROR(30002, "airdrop.wrong.rate.error"),
    OVER_AMOUNT_LIMIT(30003, "over.amount.limit"),
    AIRDROP_WRONG_OVER_USDT_LIMIT(30003, "airdrop.wrong.over.airdrop.limit"),
    AIRDROP_WRONG_OVER_BROKER_QUOTA_LIMIT(30004, "airdrop.wrong.over.broker.quota.limit"),
    USER_JOINED_ACTIVITY(30005, "user.joined.activity"),
    ORG_OPERATION_INSUFFICIENT(30006, "org.account.insufficient"),

    VERIFY_GA_ERROR(40001, "verify.google.ga.error"),
    IP_NOT_IN_WHITELIST_ERROR(40002, "ip.not.in.whitelist"),
    UNBIND_GA_ERROR(40003, "unbind.ga.error"),
    VERIFY_CAPTCHA_ERROR(40004, "bind.verify.captcha.error"),
    UNBIND_FIRST_ERROR(40005, "bind.unbind.first.error"),

    TOKEN_TYPE_ERROR(50001, "Token type error."),
    TOKEN_MISSING(50002, "Token is not exist."),
    SYMBOL_MISSING(50003, "Symbol is not exist."),
    STATE_ERROR(50004, "State error."),
    TOKEN_ALREADY_EXIST(50005, "Token exist."),
    SYMBOL_ALREADY_EXIST(50006, "Symbol exist."),
    FORBIDDEN_EDIT(50007, "Forbidden change with current state."),
    BE_DIFFERENT_WITH_QUOTE_AND_BASE(50008, "QuoteToken can not be same with baseToken."),
    UNSUPPORTED_FILE_TYPE(50009, "Unsupported file type."),
    TRY_AGAIN_LATER(50010, "Fail to execute! Retry later"),
    SYMBOL_STATE_ERROR(50011, "Symbol is not closed."),
    SYMBOL_NAME_LOCALE_REQUIRED(50012, "symbol.name.locale.required"),
    SYMBOL_RISK_LIMIT_REQUIRED(50013, "symbol.risk.limit.required"),
    SYMBOL_ID_ALREADY_EXIST(50014, "symbol.id.already.exist"),

    CUSTOM_LABEL_LOCALE_REQUIRED(50301, "custom.label.locale.required"),
    CUSTOM_LABEL_USER_ID_EMPTY(50310, "custom.label.user.id.empty"),
    CUSTOM_LABEL_USER_ID_HAVE_NOT_EXIST(50311, "custom.label.user.id.have.not.exist"),
    CUSTOM_LABEL_USER_ID_FORMAT_ERROR(50312, "custom.label.user.id.format.error"),

    SAAS_FEE_OVER_LIMIT(60001, "Saas fee should be between 0-1"),
    CALL_EXCHANGE_GATEWAY_HTTP_ERROR(60001, "call.exchange.gateway.http.error"),
    CALL_EXCHANGE_GATEWAY_ERROR(60002, "call.exchange.gateway.error"),
    PUBLISH_SYMBOL_BH_ERROR(60003, "publish.symbol.bh.error"),
    WAIT_BH_SERVER_REFRESH_CACHE(60001, "wait.bh.server.refresh.cache"),

    //CODE大于 100000的，前端要特殊处理
    SYMBOL_TOKEN_NOT_PUBLISHED(100001, ""),
    SASS_TRANSFER_ERROR_ACCOUNT(100008, ""),
    SYMBOL_IS_UPDATING(100009, ""),
    SMS_TEMPLATE_MODIFY(100010, "");

    private Integer code;

    private String desc;

    ErrorCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    public static ErrorCode valueOF(Integer code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return null;
    }
}
