package com.ciyocloud.common.constant;

/**
 * @description: redis key常量
 * @author: codeck
 * @create: 2020-02-12 22:34
 **/
public interface RedisKeyConstants {

    /**
     * 系统环境配置
     */
    String ENV_CONFIG = "config:env:{}";


    /**
     * 处理器名称
     */
    String HANDLER_NAME = "handleName";


    /**
     * redis消息通道名称
     */
    String REDIS_TOPIC_NAME = "redis:topic";
}
