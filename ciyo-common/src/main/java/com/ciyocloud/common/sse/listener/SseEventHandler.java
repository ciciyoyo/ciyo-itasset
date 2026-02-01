package com.ciyocloud.common.sse.listener;

import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.redis.listener.RedisMessage;
import com.ciyocloud.common.redis.listener.RedisMessageLister;
import com.ciyocloud.common.sse.constants.SseConstants;
import com.ciyocloud.common.sse.event.SseEvent;
import com.ciyocloud.common.sse.manager.SseConnectionManager;
import com.ciyocloud.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SSE 事件 Redis 监听器
 * 处理来自其他节点的 SSE 事件消息
 * 按用户+平台路由事件
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
            
            String userId = event.getUserId();
            // 获取平台，默认为 web
            String platform = StrUtil.isNotBlank(event.getPlatform()) 
                ? event.getPlatform() 
                : SseConstants.DEFAULT_PLATFORM;
            
            // 按平台推送事件
            boolean result = connectionManager.sendLocalEvent(userId, platform, event);
            log.debug("处理 SSE 事件消息完成: userId={}, platform={}, progressKey={}, result={}", 
                userId, platform, event.getProgressKey(), result);
        } catch (Exception e) {
            log.error("处理 SSE 事件消息失败", e);
        }
    }
}
