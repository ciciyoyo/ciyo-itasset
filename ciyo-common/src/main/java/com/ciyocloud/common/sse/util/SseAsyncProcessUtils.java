package com.ciyocloud.common.sse.util;

import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.sse.context.SseContext;
import com.ciyocloud.common.sse.event.ProcessData;
import com.ciyocloud.common.sse.event.ProcessError;
import com.ciyocloud.common.sse.service.SseEventService;
import com.ciyocloud.common.util.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步处理进度工具类
 * <p>使用前必须先调用 SseContext.init() 初始化上下文</p>
 * <p>数据结构已移至 com.ciyocloud.common.sse.event 包</p>
 *
 * @author codeck
 * @create 2021/06/08 15:35
 */
@Slf4j
public class SseAsyncProcessUtils {

    /**
     * SSE 事件服务
     */
    private static SseEventService sseEventService;

    static {
        try {
            sseEventService = SpringContextUtils.getBean(SseEventService.class);
            log.info("SseAsyncProcessUtils 初始化完成");
        } catch (Exception e) {
            log.warn("SseAsyncProcessUtils 初始化 SSE 服务失败", e);
        }
    }

    /**
     * 设置进度
     *
     * @param rate 进度百分比
     */
    public static void setProcess(int rate) {
        if (!checkContext()) return;
        var process = new ProcessData(rate, StrUtil.EMPTY);
        pushSseProgress(process);
    }

    /**
     * 设置进度带消息
     *
     * @param rate 进度百分比
     * @param msg  消息
     */
    public static void setProcess(int rate, String msg) {
        if (!checkContext()) return;
        var process = new ProcessData(rate, msg);
        pushSseProgress(process);
    }

    /**
     * 设置进度
     *
     * @param current 当前处理数
     * @param total   总数
     */
    public static void setProcess(int current, long total) {
        if (!checkContext()) return;
        var process = new ProcessData(current, total);
        pushSseProgress(process);
    }

    /**
     * 设置提示文案
     *
     * @param msg 提示消息
     */
    public static void setTips(String msg) {
        if (!checkContext()) return;
        var process = ProcessData.tipsOnly(msg);
        pushSseProgress(process);
    }

    /**
     * 设置完成
     *
     * @param message 完成消息
     */
    public static void setFinish(String message) {
        if (!checkContext()) return;
        var process = ProcessData.completeWithTips(message);
        pushSseComplete(process);
    }

    /**
     * 设置完成并返回URL
     *
     * @param url 结果URL
     */
    public static void setFinishWithUrl(String url) {
        if (!checkContext()) return;
        var process = ProcessData.completeWithUrl(url);
        pushSseComplete(process);
    }

    /**
     * 设置错误
     *
     * @param error 错误消息
     */
    public static void setError(String error) {
        if (!checkContext()) return;
        var processError = ProcessError.unknown(error);
        pushSseError(processError);
    }

    /**
     * 设置错误
     *
     * @param errorCode 错误代码
     * @param message   错误消息
     */
    public static void setError(String errorCode, String message) {
        if (!checkContext()) return;
        var error = ProcessError.builder()
                .code(errorCode)
                .message(message)
                .build();
        pushSseError(error);
    }

    // ==================== 内部方法 ====================

    private static boolean checkContext() {
        if (!SseContext.isInitialized()) {
            log.warn("SseContext 未初始化，无法推送 SSE 事件");
            return false;
        }
        return true;
    }

    private static void pushSseProgress(ProcessData process) {
        if (sseEventService != null) {
            try {
                sseEventService.sendProgress(
                    String.valueOf(SseContext.getUserId()),
                    SseContext.getPlatform(),
                    SseContext.getProgressKey(),
                    process
                );
            } catch (Exception e) {
                log.warn("推送 SSE 进度事件失败", e);
            }
        }
    }

    private static void pushSseComplete(ProcessData process) {
        if (sseEventService != null) {
            try {
                sseEventService.sendComplete(
                    SseContext.getUserId(),
                    SseContext.getPlatform(),
                    SseContext.getProgressKey(),
                    process
                );
            } catch (Exception e) {
                log.warn("推送 SSE 完成事件失败", e);
            }
        }
    }

    private static void pushSseError(ProcessError error) {
        if (sseEventService != null) {
            try {
                sseEventService.sendError(
                    String.valueOf(SseContext.getUserId()),
                    SseContext.getPlatform(),
                    SseContext.getProgressKey(),
                    error
                );
            } catch (Exception e) {
                log.warn("推送 SSE 错误事件失败", e);
            }
        }
    }
}
