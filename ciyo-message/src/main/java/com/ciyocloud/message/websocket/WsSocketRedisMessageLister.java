package com.ciyocloud.message.websocket;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.ciyocloud.common.redis.listener.RedisMessage;
import com.ciyocloud.common.redis.listener.RedisMessageLister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.util.Map;

/**
 * 监听消息(采用redis发布订阅方式发送消息)
 */
@Slf4j
@Component
public class WsSocketRedisMessageLister implements RedisMessageLister {


    @Override
    public void onMessage(RedisMessage message) {
        Map<String, String> params = Convert.toMap(String.class, String.class, message.getContent());
        log.info("SocketHandler消息 Redis Lister:" + params.toString());

        String userId = params.get("userId");
        String messageStr = params.get("message");
        if (ObjectUtil.isNotEmpty(userId)) {
            WsSessionManager.pushMessage(userId, new TextMessage(messageStr));
        } else {
            WsSessionManager.pushMessage(new TextMessage(messageStr));
        }
    }
}
