package com.ciyocloud.common.sse.service;

import com.ciyocloud.common.constant.RedisKeyConstants;
import com.ciyocloud.common.redis.listener.RedisMessage;
import com.ciyocloud.common.sse.context.SseContext;
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
 * 使用 SseContext 获取 userId, platform, progressKey
 *
 * @author codeck
 * @create 2026/01/28
 */
@Slf4j
@Service
public class SseEventService {

    private static final String SSE_EVENT_CHANNEL = "sse:events";

    @Autowired
    private SseConnectionManager connectionManager;
    @Autowired
    private RedisUtils redisUtils;

    // ==================== 基于上下文的简化方法 ====================

    /**
     * 推送进度事件（从上下文获取参数）
     */
    public void sendProgress(Object progressData) {
        if (!SseContext.isInitialized()) {
            log.warn("SseContext 未初始化，无法推送进度事件");
            return;
        }
        String userId = String.valueOf(SseContext.getUserId());
        String platform = SseContext.getPlatform();
        String progressKey = SseContext.getProgressKey();

        SseEvent event = SseEvent.progress(userId, progressKey, progressData);
        doSend(userId, platform, progressKey, event);
    }

    /**
     * 推送完成事件（从上下文获取参数）
     */
    public void sendComplete(Object result) {
        if (!SseContext.isInitialized()) {
            log.warn("SseContext 未初始化，无法推送完成事件");
            return;
        }
        String userId = String.valueOf(SseContext.getUserId());
        String platform = SseContext.getPlatform();
        String progressKey = SseContext.getProgressKey();

        SseEvent event = SseEvent.complete(userId, progressKey, result);
        doSend(userId, platform, progressKey, event);
    }

    /**
     * 推送错误事件（从上下文获取参数）
     */
    public void sendError(Object errorData) {
        if (!SseContext.isInitialized()) {
            log.warn("SseContext 未初始化，无法推送错误事件");
            return;
        }
        String userId = String.valueOf(SseContext.getUserId());
        String platform = SseContext.getPlatform();
        String progressKey = SseContext.getProgressKey();

        SseEvent event = SseEvent.error(userId, progressKey, errorData);
        doSend(userId, platform, progressKey, event);
    }

    // ==================== 内部方法（供 SseAsyncProcessUtils 调用） ====================

    /**
     * 推送进度事件（显式参数，内部使用）
     */
    public void sendProgress(String userId, String platform, String progressKey, Object progressData) {
        SseEvent event = SseEvent.progress(userId, progressKey, progressData);
        doSend(userId, platform, progressKey, event);
    }

    /**
     * 推送完成事件（显式参数，内部使用）
     */
    public void sendComplete(Long userId, String platform, String progressKey, Object result) {
        SseEvent event = SseEvent.complete(String.valueOf(userId), progressKey, result);
        doSend(String.valueOf(userId), platform, progressKey, event);
    }

    /**
     * 推送错误事件（显式参数，内部使用）
     */
    public void sendError(String userId, String platform, String progressKey, Object errorData) {
        SseEvent event = SseEvent.error(userId, progressKey, errorData);
        doSend(userId, platform, progressKey, event);
    }

    // ==================== 核心发送逻辑 ====================

    private void doSend(String userId, String platform, String progressKey, SseEvent event) {
        event.setProgressKey(progressKey);
        event.setPlatform(platform);

        if (connectionManager.hasLocalConnection(userId, platform)) {
            sendLocalEvent(userId, platform, event);
        } else {
            sendRemoteEvent(userId, platform, event);
        }
    }

    private void sendLocalEvent(String userId, String platform, SseEvent event) {
        try {
            boolean result = connectionManager.sendLocalEvent(userId, platform, event);
            if (result) {
                log.debug("本地 SSE 推送成功: userId={}, platform={}, progressKey={}",
                    userId, platform, event.getProgressKey());
            } else {
                log.warn("本地 SSE 推送失败: userId={}, platform={}, progressKey={}",
                    userId, platform, event.getProgressKey());
            }
        } catch (Exception e) {
            log.error("本地 SSE 推送异常: userId={}, platform={}, progressKey={}",
                userId, platform, event.getProgressKey(), e);
        }
    }

    private void sendRemoteEvent(String userId, String platform, SseEvent event) {
        try {
            RedisMessage redisMessage = new RedisMessage();
            redisMessage.setHandlerName("sseEventHandler");
            redisMessage.setContent(JsonUtils.objToJson(event));
            SpringContextUtils.getBean(RedisUtils.class).getRedisTemplate()
                .convertAndSend(RedisKeyConstants.REDIS_TOPIC_NAME, redisMessage);
            log.debug("远程 SSE 推送成功: userId={}, platform={}, progressKey={}",
                userId, platform, event.getProgressKey());
        } catch (Exception e) {
            log.error("远程 SSE 推送失败: userId={}, platform={}, progressKey={}",
                userId, platform, event.getProgressKey(), e);
        }
    }

    // ==================== 管理方法 ====================

    public Map<String, Object> getConnectionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("localConnections", connectionManager.getLocalConnectionCount());
        return stats;
    }

    public void closeConnection(String userId, String platform) {
        if (connectionManager.hasLocalConnection(userId, platform)) {
            connectionManager.closeConnection(userId, platform);
        } else {
            Map<String, String> message = new HashMap<>();
            message.put(RedisKeyConstants.HANDLER_NAME, "sseEventHandler");
            message.put("type", "closeConnection");
            message.put("userId", userId);
            message.put("platform", platform);
            redisUtils.getRedisTemplate().convertAndSend(SSE_EVENT_CHANNEL, message);
        }
    }
}
