package com.ciyocloud.common.exception;


/**
 * 无业务访问权限
 */
public class NoAccessException extends BaseException {
    public NoAccessException(String msg) {
        super(msg);
        this.code = 4010;
    }
}
