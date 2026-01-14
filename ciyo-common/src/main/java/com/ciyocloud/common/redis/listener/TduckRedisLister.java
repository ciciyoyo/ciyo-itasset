package com.ciyocloud.common.redis.listener;


import java.util.Map;

/**
 * 自定义消息监听
 */
public interface TduckRedisLister {

    void onMessage(Map<String, String> params);

}
