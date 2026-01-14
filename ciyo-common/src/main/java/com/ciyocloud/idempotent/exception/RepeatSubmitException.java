package com.ciyocloud.idempotent.exception;

import com.ciyocloud.common.exception.BaseException;

/**
 * 重复提交异常类
 *
 * @author codeck
 */
public class RepeatSubmitException extends BaseException {
    public RepeatSubmitException(String msg) {
        super(msg);
    }
}
