package io.bhex.bhop.common.exception;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.exception
 * @Author: ming.xu
 * @CreateDate: 15/10/2018 3:41 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public class ErrorCaptchaException extends Exception {

    public ErrorCaptchaException() {
        super();
    }

    public ErrorCaptchaException(String message) {
        super(message);
    }

    public ErrorCaptchaException(String message, Throwable cause) {
        super(message, cause);
    }
}
