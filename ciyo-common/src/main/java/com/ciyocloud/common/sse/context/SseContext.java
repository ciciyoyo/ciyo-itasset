package com.ciyocloud.common.sse.context;

import com.ciyocloud.common.sse.constants.SseConstants;
import lombok.Data;

/**
 * SSE 上下文
 * 使用 ThreadLocal 存储当前线程的 SSE 相关信息，避免参数层层传递
 *
 * @author codeck
 * @create 2026/01/31
 */
public class SseContext {

    private static final ThreadLocal<SseContextInfo> CONTEXT = new ThreadLocal<>();

    /**
     * 初始化上下文（使用默认平台）
     *
     * @param userId      用户ID
     * @param progressKey 进度标识
     */
    public static void init(Long userId, String progressKey) {
        init(userId, progressKey, SseConstants.DEFAULT_PLATFORM);
    }

    /**
     * 初始化上下文
     *
     * @param userId      用户ID
     * @param progressKey 进度标识
     * @param platform    平台标识
     */
    public static void init(Long userId, String progressKey, String platform) {
        SseContextInfo info = new SseContextInfo();
        info.setUserId(userId);
        info.setProgressKey(progressKey);
        info.setPlatform(platform != null ? platform : SseConstants.DEFAULT_PLATFORM);
        CONTEXT.set(info);
    }

    /**
     * 获取当前上下文
     */
    public static SseContextInfo get() {
        return CONTEXT.get();
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        SseContextInfo info = CONTEXT.get();
        return info != null ? info.getUserId() : null;
    }

    /**
     * 获取进度标识
     */
    public static String getProgressKey() {
        SseContextInfo info = CONTEXT.get();
        return info != null ? info.getProgressKey() : null;
    }

    /**
     * 获取平台标识
     */
    public static String getPlatform() {
        SseContextInfo info = CONTEXT.get();
        return info != null ? info.getPlatform() : SseConstants.DEFAULT_PLATFORM;
    }

    /**
     * 检查上下文是否已初始化
     */
    public static boolean isInitialized() {
        SseContextInfo info = CONTEXT.get();
        return info != null && info.getUserId() != null && info.getProgressKey() != null;
    }

    /**
     * 清除上下文（务必在 finally 中调用）
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 执行带上下文的任务
     *
     * @param userId      用户ID
     * @param progressKey 进度标识
     * @param task        任务
     */
    public static void execute(Long userId, String progressKey, Runnable task) {
        execute(userId, progressKey, SseConstants.DEFAULT_PLATFORM, task);
    }

    /**
     * 执行带上下文的任务
     *
     * @param userId      用户ID
     * @param progressKey 进度标识
     * @param platform    平台标识
     * @param task        任务
     */
    public static void execute(Long userId, String progressKey, String platform, Runnable task) {
        init(userId, progressKey, platform);
        try {
            task.run();
        } finally {
            clear();
        }
    }

    /**
     * SSE 上下文信息
     */
    @Data
    public static class SseContextInfo {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 进度标识
         */
        private String progressKey;

        /**
         * 平台标识
         */
        private String platform;
    }
}
