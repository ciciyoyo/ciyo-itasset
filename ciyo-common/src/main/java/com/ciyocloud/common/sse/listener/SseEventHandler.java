package com.ciyocloud.common.sse.listener;

import com.ciyocloud.common.redis.listener.RedisMessage;
import com.ciyocloud.common.redis.listener.RedisMessageLister;
import com.ciyocloud.common.sse.event.SseEvent;
import com.ciyocloud.common.sse.manager.SseConnectionManager;
import com.ciyocloud.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SSE 事件 Redis 监听器
 * 处理来自其他节点的 SSE 事件消息
 *
 * @author codeck
 * @create 2026/01/28
 */
@Slf4j
@Component("sseEventHandler")
public class SseEventHandler implements RedisMessageLister {

    @Autowired
    private SseConnectionManager connectionManager;

    @Override
    public void onMessage(RedisMessage message) {
        try {
            log.debug("收到 SSE 事件消息: {}", message.getContent());
            SseEvent event = JsonUtils.jsonToObj(message.getContent().toString(), SseEvent.class);
            if (event == null) return;
            boolean result = connectionManager.sendLocalEvent(event.getUserId(), event.getProgressKey(), event);
        } catch (Exception e) {
            log.error("处理 SSE 事件消息失败", e);
        }
    }
}
