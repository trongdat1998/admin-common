package io.bhex.bhop.common.exception;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.exception
 * @Author: ming.xu
 * @CreateDate: 15/10/2018 3:25 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public class UserNotExistException extends RuntimeException {

    public UserNotExistException() {
        super();
    }

    public UserNotExistException(String message) {
        super(message);
    }

    public UserNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotExistException(Throwable cause) {
        super(cause);
    }
}
