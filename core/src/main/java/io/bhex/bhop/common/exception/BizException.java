package io.bhex.bhop.common.exception;

/**
 * @Description: 系统业务异常，业务抛出此异常 由GlobalExceptionHandler统一包装返回给api调用方
 * @Date: 2018/8/10 下午1:57
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = -2623309261327598087L;

    private int code;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getDesc());
        setCode(errorCode.getCode());
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        setCode(errorCode.getCode());
    }

    public BizException(String message){
        this(ErrorCode.ERROR,message);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
