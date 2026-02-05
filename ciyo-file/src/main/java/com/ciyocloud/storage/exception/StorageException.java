package com.ciyocloud.storage.exception;

import com.ciyocloud.common.exception.BaseException;

/**
 * @author : codeck
 * @description : 异常
 * @create : 2020-11-10 10:49
 **/
public class StorageException extends BaseException {
    public StorageException(String msg) {
        super(msg);
    }

    public StorageException(String msg, Throwable e) {
        super(msg, e);
    }
}
