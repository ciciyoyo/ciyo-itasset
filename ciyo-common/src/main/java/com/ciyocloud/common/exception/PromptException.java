package com.ciyocloud.common.exception;

/**
 * 提示型异常
 *
 * @author : codeck
 * @since :  2025/07/30 20:50
 **/
public class PromptException extends BaseException {
    private static final long serialVersionUID = 1L;

    public PromptException(String msg) {
        super(msg, 100);
    }
}
