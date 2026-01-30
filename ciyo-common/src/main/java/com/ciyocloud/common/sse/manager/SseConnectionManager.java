package com.ciyocloud.common.sse.manager;

import com.ciyocloud.common.sse.constants.SseConstants;
import com.ciyocloud.common.sse.event.SseEvent;
import com.ciyocloud.common.util.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 统一的 SSE 连接管理器
 * 集成了本地和分布式SSE连接管理功能，支持跨节点的事件分发和本地连接维护
 *
 * @author codeck
 * @create 2026/01/29
 */
@Slf4j
@Component
public class SseConnectionManager {
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;
    /**
     * 重试间隔(毫秒)
     */
    private static final long RETRY_INTERVAL_MS = 1000;
    /**
     * 本地 SSE 连接存储
     * Key: connectionKey.toString(), Value: SseEmitter
     */
    private final Map<String, SseEmitter> connections = new ConcurrentHashMap<>();
    /**
     * 连接健康状态记录
     * Key: connectionKey, Value: ConnectionHealth
     */
    private final Map<String, ConnectionHealth> connectionHealthMap = new ConcurrentHashMap<>();
    /**
     * 心跳调度器
     */
    private final ScheduledExecutorService heartbeatExecutor;

    public SseConnectionManager() {
        this.heartbeatExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r, "sse-heartbeat");
            thread.setDaemon(true);
            return thread;
        });

        startHeartbeatTask();
        log.info("初始化统一SSE连接管理器");
    }

    /**
     * 创建 SSE 连接（本地+分布式注册）
     *
     * @param userId      用户ID
     * @param progressKey 进度键
     * @return SSE连接对象
     * @throws RuntimeException 连接创建失败时抛出
     */
    public SseEmitter createConnection(String userId, String progressKey) {
        var key = buildConnectionKey(userId, progressKey);

        try {
            // 1. 如果已存在本地连接，先关闭旧连接
            closeLocalConnection(key);

            // 2. 创建新的 SSE 连接
            var emitter = new SseEmitter(SseConstants.SSE_TIMEOUT_MILLIS);

            // 3. 设置连接回调并存储连接
            setupEmitterAndStore(emitter, userId, progressKey, key);

            logConnectionSuccess(userId, progressKey);
            return emitter;
        } catch (Exception e) {
            log.error("创建SSE连接失败: userId={}, progressKey={}", userId, progressKey, e);
            cleanup(userId, progressKey);
            throw new RuntimeException("创建SSE连接失败: " + e.getMessage(), e);
        }
    }


    /**
     * 发送事件到指定本地连接
     *
     * @param userId      用户ID
     * @param progressKey 进度键
     * @param event       事件对象
     * @return 发送是否成功
     */
    public boolean sendLocalEvent(String userId, String progressKey, SseEvent event) {
        var key = buildConnectionKey(userId, progressKey);
        var emitter = connections.get(key);

        if (emitter == null) {
            log.warn("本地SSE连接不存在: userId={}, progressKey={}", userId, progressKey);
            return false;
        }

        // 使用重试机制发送事件
        return sendEventWithRetry(key, emitter, event, userId, progressKey);
    }

    /**
     * 关闭连接
     *
     * @param userId      用户ID
     * @param progressKey 进度键
     */
    public void closeConnection(String userId, String progressKey) {
        cleanup(userId, progressKey);
        log.info("关闭SSE连接: userId={}, progressKey={}", userId, progressKey);
    }


    /**
     * 检查本地连接是否存在
     *
     * @param userId      用户ID
     * @param progressKey 进度键
     * @return true 如果连接存在
     */
    public boolean hasLocalConnection(String userId, String progressKey) {
        return connections.containsKey(buildConnectionKey(userId, progressKey));
    }

    /**
     * 获取本地连接数量
     *
     * @return 连接数量
     */
    public int getLocalConnectionCount() {
        return connections.size();
    }


    /**
     * 记录连接成功日志
     */
    private void logConnectionSuccess(String userId, String progressKey) {
        log.info("创建SSE连接成功: userId={}, progressKey={}, node={}, 当前连接数={}", userId, progressKey, "local-node", connections.size());
    }


    /**
     * 清理连接资源
     */
    private void cleanup(String userId, String progressKey) {
        try {
            // 关闭本地连接
            closeLocalConnection(buildConnectionKey(userId, progressKey));
        } catch (Exception e) {
            log.error("清理连接资源失败: userId={}, progressKey={}", userId, progressKey, e);
        }
    }

    /**
     * 设置 SSE Emitter 回调
     */
    private void setupEmitterCallbacks(SseEmitter emitter, String userId, String progressKey) {
        var key = buildConnectionKey(userId, progressKey);

        // 统一的清理逻辑
        Runnable cleanup = () -> {
            removeLocalConnection(key);
        };

        emitter.onCompletion(() -> {
            log.debug("SSE连接完成: userId={}, progressKey={}", userId, progressKey);
            cleanup.run();
        });

        emitter.onTimeout(() -> {
            log.debug("SSE连接超时: userId={}, progressKey={}", userId, progressKey);
            cleanup.run();
        });

        emitter.onError(throwable -> {
            log.error("SSE连接错误: userId={}, progressKey={}", userId, progressKey, throwable);
            cleanup.run();
        });
    }

    /**
     * 发送连接成功事件
     */
    private void sendConnectionSuccessEvent(SseEmitter emitter, String userId, String progressKey) {
        try {
            emitter.send(SseEmitter.event().id(String.valueOf(System.currentTimeMillis())).name("connected").data("SSE连接建立成功").reconnectTime(SseConstants.DEFAULT_RECONNECT_TIME));
            log.debug("发送连接成功事件成功: userId={}, progressKey={}", userId, progressKey);
        } catch (IOException e) {
            // 发送连接成功事件失败不应该导致连接创建失败，只记录警告日志
            log.warn("发送连接成功事件失败: userId={}, progressKey={}, 但连接仍然有效", userId, progressKey, e);
        }
    }

    /**
     * 关闭本地连接（内部方法）
     */
    private void closeLocalConnection(String key) {
        SseEmitter emitter = connections.get(key);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("关闭SSE连接异常: {}", key, e);
            }
            removeLocalConnection(key);
        }
    }

    /**
     * 移除本地连接
     */
    private void removeLocalConnection(String key) {
        connections.remove(key);
        connectionHealthMap.remove(key);
        log.debug("移除本地SSE连接: {}, 剩余连接数={}", key, connections.size());
    }

    /**
     * 构建连接键
     */
    private String buildConnectionKey(String userId, String progressKey) {
        return userId + ":" + progressKey;
    }



    /**
     * 启动心跳任务
     */
    private void startHeartbeatTask() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                sendHeartbeat();
            } catch (Exception e) {
                log.error("心跳任务执行失败", e);
            }
        }, SseConstants.HEARTBEAT_INTERVAL_SECONDS, SseConstants.HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);

        log.info("启动SSE心跳任务，间隔: {}秒", SseConstants.HEARTBEAT_INTERVAL_SECONDS);
    }

    /**
     * 设置 Emitter 并存储连接
     */
    private void setupEmitterAndStore(SseEmitter emitter, String userId, String progressKey, String key) {
        setupEmitterCallbacks(emitter, userId, progressKey);
        connections.put(key, emitter);
        sendConnectionSuccessEvent(emitter, userId, progressKey);
    }

    /**
     * 创建事件构建器
     */
    private SseEmitter.SseEventBuilder createEventBuilder(SseEvent event) {
        var eventData = JsonUtils.objToJson(event.getData());
        var eventBuilder = SseEmitter.event().id(event.getId()).name(event.getEvent()).data(eventData, MediaType.APPLICATION_JSON);

        return event.getRetry() != null ? eventBuilder.reconnectTime(event.getRetry()) : eventBuilder;
    }

    /**
     * 发送心跳
     */
    private void sendHeartbeat() {
        if (connections.isEmpty()) {
            return;
        }

        int failedCount = 0;
        int recoveredCount = 0;

        for (Map.Entry<String, SseEmitter> entry : connections.entrySet()) {
            String key = entry.getKey();
            ConnectionHealth health = connectionHealthMap.get(key);

            if (health == null) {
                health = new ConnectionHealth();
                connectionHealthMap.put(key, health);
            }

            try {
                // 为每个连接创建新的心跳事件，避免事件构建器状态复用问题
                SseEmitter.SseEventBuilder heartbeatEvent = createHeartbeatEvent();
                entry.getValue().send(heartbeatEvent);

                // 发送成功，重置失败计数
                if (health.getConsecutiveFailures() > 0) {
                    log.info("连接 {} 恢复正常，重置失败计数", key);
                    recoveredCount++;
                }
                health.resetFailures();

            } catch (IOException | IllegalStateException e) {
                health.incrementFailures();
                log.warn("发送心跳失败，连接: {}，连续失败次数: {}", key, health.getConsecutiveFailures());

                // 只有连续失败次数超过阈值才移除连接
                if (health.getConsecutiveFailures() >= MAX_RETRY_COUNT) {
                    log.error("连接 {} 连续失败 {} 次，移除连接", key, health.getConsecutiveFailures());
                    removeLocalConnection(key);
                    connectionHealthMap.remove(key);
                }
                failedCount++;
            }
        }

        if (recoveredCount > 0 || failedCount > 0) {
            log.debug("心跳统计 - 总连接数: {}, 失败: {}, 恢复: {}", connections.size(), failedCount, recoveredCount);
        }
    }

    /**
     * 创建心跳事件
     */
    private SseEmitter.SseEventBuilder createHeartbeatEvent() {
        return SseEmitter.event().id(String.valueOf(System.currentTimeMillis())).name("heartbeat").data("ping");
    }

    /**
     * 带重试机制的事件发送
     */
    private boolean sendEventWithRetry(String key, SseEmitter emitter, SseEvent event, String userId, String progressKey) {
        ConnectionHealth health = connectionHealthMap.computeIfAbsent(key, k -> new ConnectionHealth());

        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                var eventBuilder = createEventBuilder(event);
                emitter.send(eventBuilder);

                // 发送成功，重置失败计数
                if (health.getConsecutiveFailures() > 0) {
                    log.info("连接 {} 事件发送恢复正常，重置失败计数", key);
                }
                health.resetFailures();

                log.debug("发送SSE事件成功: userId={}, progressKey={}, event={}, attempt={}", userId, progressKey, event.getEvent(), attempt);
                return true;

            } catch (IllegalStateException e) {
                log.warn("发送SSE事件失败: userId={}, progressKey={}, 连接已关闭", userId, progressKey, e);
                removeLocalConnection(key);
                connectionHealthMap.remove(key);
                return false;
            } catch (IOException e) {
                health.incrementFailures();
                log.warn("发送SSE事件失败: userId={}, progressKey={}, attempt={}/{}, 连续失败次数={}",
                        userId, progressKey, attempt, MAX_RETRY_COUNT, health.getConsecutiveFailures(), e);

                // 如果不是最后一次尝试，等待后重试
                if (attempt < MAX_RETRY_COUNT) {
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    // 最后一次尝试也失败了，检查是否需要移除连接
                    if (health.getConsecutiveFailures() >= MAX_RETRY_COUNT) {
                        log.error("连接 {} 事件发送连续失败 {} 次，移除连接", key, health.getConsecutiveFailures());
                        removeLocalConnection(key);
                        connectionHealthMap.remove(key);
                    }
                }
            } catch (Exception e) {
                log.error("序列化事件数据失败: userId={}, progressKey={}", userId, progressKey, e);
                return false;
            }
        }

        return false;
    }

    /**
     * 连接健康状态类
     */
    private static class ConnectionHealth {
        @Getter
        private volatile int consecutiveFailures = 0;

        public void incrementFailures() {
            consecutiveFailures++;
        }

        public void resetFailures() {
            consecutiveFailures = 0;
        }
    }

    /**
     * 发送结果 - 使用 record 简化代码
     */
    public record SendResult(boolean success, String type, String targetNode, String error) {

        public static SendResult of(boolean success, String type, String targetNode, String error) {
            return new SendResult(success, type, targetNode, error);
        }

        public static SendResult error(String error) {
            return of(false, "error", null, error);
        }

    }

}
