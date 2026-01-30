package com.ciyocloud.common.exception;

/**
 * @description: 业务异常
 * @author: codeck
 * @create: 2018-09-13 10:19
 **/
public class BusinessException extends BaseException {
    private static final long serialVersionUID = 1L;
    private final int code = 501;
    private String msg;


    public BusinessException(String msg) {
        super(msg);
    }
}
