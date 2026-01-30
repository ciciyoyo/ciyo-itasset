package com.ciyocloud.common.sse.service;

import com.ciyocloud.common.constant.RedisKeyConstants;
import com.ciyocloud.common.redis.listener.RedisMessage;
import com.ciyocloud.common.sse.event.SseEvent;
import com.ciyocloud.common.sse.manager.SseConnectionManager;
import com.ciyocloud.common.util.JsonUtils;
import com.ciyocloud.common.util.RedisUtils;
import com.ciyocloud.common.util.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * SSE 事件推送服务
 * 负责处理分布式环境下的 SSE 事件推送
 *
 * @author codeck
 * @create 2026/01/28
 */
@Slf4j
@Service
public class SseEventService {

    /**
     * SSE 事件 Redis Channel
     */
    private static final String SSE_EVENT_CHANNEL = "sse:events";
    @Autowired
    private SseConnectionManager connectionManager;
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 推送事件到指定用户
     */
    public void sendToUser(String userId, String progressKey, SseEvent event) {
        // 简化逻辑：优先检查本地连接，有就直接推送，没有就远程推送
        if (connectionManager.hasLocalConnection(userId, progressKey)) {
            // 本地有连接，直接推送
            sendLocalEvent(userId, progressKey, event);
        } else {
            // 本地没有连接，通过 Redis 远程推送
            sendRemoteEvent(userId, progressKey, event);
        }
    }

    /**
     * 推送进度事件
     */
    public void sendProgress(String userId, String progressKey, Object progressData) {
        SseEvent event = SseEvent.progress(userId, progressKey, progressData);
        sendToUser(userId, progressKey, event);
    }

    /**
     * 推送完成事件
     */
    public void sendComplete(Long userId, String progressKey, Object result) {
        SseEvent event = SseEvent.complete(String.valueOf(userId), progressKey, result);
        sendToUser(String.valueOf(userId), progressKey, event);
    }

    /**
     * 推送错误事件（结构化错误对象）
     */
    public void sendError(String userId, String progressKey, Object errorData) {
        SseEvent event = SseEvent.error(userId, progressKey, errorData);
        sendToUser(userId, progressKey, event);
    }

    /**
     * 本地事件推送
     */
    private void sendLocalEvent(String userId, String progressKey, SseEvent event) {
        try {
            boolean result = connectionManager.sendLocalEvent(userId, progressKey, event);
            if (result) {
                log.debug("本地 SSE 事件推送成功: userId={}, progressKey={}, event={}", userId, progressKey, event.getEvent());
            } else {
                log.warn("本地 SSE 事件推送失败: userId={}, progressKey={}, event={}, error={}", userId, progressKey, event.getEvent(), "No active connections");
            }
        } catch (Exception e) {
            log.error("本地 SSE 事件推送异常: userId={}, progressKey={}, event={}", userId, progressKey, event.getEvent(), e);
        }
    }

    /**
     * 远程事件推送（通过 Redis）
     */
    private void sendRemoteEvent(String userId, String progressKey, SseEvent event) {
        try {
            RedisMessage redisMessage = new RedisMessage();
            redisMessage.setHandlerName("sseEventHandler");
            redisMessage.setContent(JsonUtils.objToJson(event));
            SpringContextUtils.getBean(RedisUtils.class).getRedisTemplate().convertAndSend(RedisKeyConstants.REDIS_TOPIC_NAME, redisMessage);
            log.debug("远程 SSE 事件推送成功: userId={}, progressKey={}, event={}", userId, progressKey, event.getEvent());
        } catch (Exception e) {
            log.error("远程 SSE 事件推送失败: userId={}, progressKey={}, event={}", userId, progressKey, event.getEvent(), e);
        }
    }


    /**
     * 获取连接统计信息
     */
    public Map<String, Object> getConnectionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("localConnections", connectionManager.getLocalConnectionCount());
        return stats;
    }

    /**
     * 关闭用户连接
     */
    public void closeConnection(String userId, String progressKey) {
        // 优先关闭本地连接
        if (connectionManager.hasLocalConnection(userId, progressKey)) {
            connectionManager.closeConnection(userId, progressKey);
        } else {
            // 本地没有，通知其他节点关闭
            Map<String, String> message = new HashMap<>();
            message.put(RedisKeyConstants.HANDLER_NAME, "sseEventHandler");
            message.put("type", "closeConnection");
            message.put("userId", userId);
            message.put("progressKey", progressKey);
            redisUtils.getRedisTemplate().convertAndSend(SSE_EVENT_CHANNEL, message);
        }
    }
}
