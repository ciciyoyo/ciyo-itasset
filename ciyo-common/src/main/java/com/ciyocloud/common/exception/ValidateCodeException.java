package com.ciyocloud.common.exception;

/**
 * @author : codeck
 * @description : 验证码错误
 * @create : 2020-12-14 16:00
 **/
public class ValidateCodeException extends BaseException {
    public ValidateCodeException(String msg) {
        super(msg);
    }
}
