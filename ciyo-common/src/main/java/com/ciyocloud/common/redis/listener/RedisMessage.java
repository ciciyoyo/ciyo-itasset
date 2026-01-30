package com.ciyocloud.common.redis.listener;


import lombok.Data;

/**
 * redis 消息
 */
@Data
public class RedisMessage {

    /**
     * 处理消息handlername
     */
    private String handlerName;


    /**
     * 消息内容
     */
    private Object content;


}
