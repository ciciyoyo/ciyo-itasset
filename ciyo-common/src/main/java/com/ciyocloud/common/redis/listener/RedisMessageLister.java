package com.ciyocloud.common.redis.listener;



/**
 * 自定义消息监听
 */
public interface RedisMessageLister {


    /**
     * redis 消息监听
     * @param message 消息参数
     */
    void onMessage(RedisMessage message);

}
