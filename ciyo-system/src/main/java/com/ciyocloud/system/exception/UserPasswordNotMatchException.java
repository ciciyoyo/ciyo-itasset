package com.ciyocloud.system.exception;

import com.ciyocloud.common.exception.BaseException;

/**
 * 用户密码不正确或不符合规范异常类
 *
 * @author codeck
 */
public class UserPasswordNotMatchException extends BaseException {

    public UserPasswordNotMatchException(String msg) {
        super(msg);
    }
}
