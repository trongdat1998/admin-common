package io.bhex.bhop.common.exception;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.exception
 * @Author: ming.xu
 * @CreateDate: 15/10/2018 1:53 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public class OneMinuteDuplicateException extends RuntimeException {

    public OneMinuteDuplicateException() {
        super();
    }

    public OneMinuteDuplicateException(String message) {
        super(message);
    }

    public OneMinuteDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }
}
