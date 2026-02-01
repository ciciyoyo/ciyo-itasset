package com.ciyocloud.common.sse.constants;

/**
 * SSE 相关常量
 *
 * @author codeck
 * @create 2026/01/29
 */
public class SseConstants {

    /**
     * 连接映射 Redis Key 前缀
     */
    public static final String CONNECTION_KEY_PREFIX = "sse:connection:";

    /**
     * 节点连接列表 Redis Key 前缀
     */
    public static final String NODE_CONNECTIONS_KEY_PREFIX = "sse:node:";

    /**
     * SSE 连接超时时间（毫秒）
     */
    public static final long SSE_TIMEOUT_MILLIS = 300 * 1000L; // 5 分钟

    /**
     * Redis 连接超时时间（秒）
     */
    public static final long REDIS_TIMEOUT_SECONDS = 300L; // 5 分钟

    /**
     * 心跳间隔（秒）
     */
    public static final long HEARTBEAT_INTERVAL_SECONDS = 30L;

    /**
     * 默认重连时间（毫秒）
     */
    public static final long DEFAULT_RECONNECT_TIME = 3000L;

    /**
     * 平台请求头名称
     */
    public static final String PLATFORM_HEADER = "X-Platform";

    /**
     * 默认平台
     */
    public static final String DEFAULT_PLATFORM = "web";

    private SseConstants() {
        // 工具类，禁止实例化
    }
}