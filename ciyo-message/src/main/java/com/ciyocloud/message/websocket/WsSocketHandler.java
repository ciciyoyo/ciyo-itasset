package com.ciyocloud.message.websocket;

import cn.hutool.core.util.ObjectUtil;
import com.ciyocloud.common.redis.listener.TduckRedisLister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.util.Map;

/**
 * 监听消息(采用redis发布订阅方式发送消息)
 */
@Slf4j
@Component
public class WsSocketHandler implements TduckRedisLister {


    @Override
    public void onMessage(Map<String, String> params) {
        log.info("SocketHandler消息 Redis Lister:" + params.toString());

        String userId = params.get("userId");
        String message = params.get("message");
        if (ObjectUtil.isNotEmpty(userId)) {
            WsSessionManager.pushMessage(userId, new TextMessage(message));
        } else {
            WsSessionManager.pushMessage(new TextMessage(message));
        }
    }
}
