package com.ciyocloud.common.sse.util;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.sse.service.SseEventService;
import com.ciyocloud.common.util.SpringContextUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * @author : codeck
 * @description : 异步处理进度 像导出文件等异步耗时操作，支持 SSE 推送
 * @create :  2021/06/08 15:35
 **/

@Slf4j
public class SseAsyncProcessUtils {

    /**
     * 最大完成率
     */
    private final static int MAX_PROCESS_RATE = 100;

    /**
     * SSE 事件服务
     */
    private static SseEventService sseEventService;

    static {
        // 初始化SSE事件服务
        try {
            sseEventService = SpringContextUtils.getBean(SseEventService.class);
            log.info("SseAsyncProcessUtils 初始化完成，仅支持 SSE 推送");
        } catch (Exception e) {
            log.warn("SseAsyncProcessUtils 初始化 SSE 服务失败", e);
        }
    }

    /**
     * 设置进度（支持 SSE 推送）
     *
     * @param key    唯一表示
     * @param rate   进度百分比
     * @param userId 用户ID（用于 SSE 推送）
     */
    public static void setProcess(String key, int rate, Long userId) {
        Process process = new Process(rate, StrUtil.EMPTY);
        pushSseProgress(userId, key, process);
    }

    /**
     * 设置进度（支持 SSE 推送）
     *
     * @param key    唯一表示
     * @param rate   进度百分比
     * @param userId 用户ID（用于 SSE 推送）
     */
    public static void setProcess(String key, int rate, Long userId, String msg) {
        Process process = new Process(rate, msg);
        pushSseProgress(userId, key, process);
    }


    /**
     * 设置进度（支持 SSE 推送）
     *
     * @param key     唯一表示
     * @param current 当前处理数
     * @param total   总数
     * @param userId  用户ID（用于 SSE 推送）
     */
    public static void setProcess(String key, int current, long total, Long userId) {
        Process process = new Process(current, total);
        pushSseProgress(userId, key, process);
    }


    /**
     * 执行进度完成（支持 SSE 推送）
     *
     * @param key    唯一标识
     * @param url    结果URL
     * @param userId 用户ID（用于 SSE 推送）
     */
    public static void setProcessUrlFinish(String key, String url, Long userId) {
        Process process = new Process(MAX_PROCESS_RATE, url);
        pushSseComplete(userId, key, process);
    }

    public static void setProcessFinish(String key, String message, Long userId) {
        Process process = new Process();
        process.setRate(MAX_PROCESS_RATE);
        process.setTips(message);
        pushSseComplete(userId, key, process);
    }


    /**
     * 设置提示文案（支持 SSE 推送）
     */
    public static void setProcessTips(String key, String msg, Long userId) {
        Process process = new Process(0, "");
        process.tips = msg;
        pushSseProgress(userId, key, process);
    }

    /**
     * 设置提示文案（支持 SSE 推送）
     */
    public static void setProcessTips(String key, String msg) {
        setProcessTips(key, msg, null);
    }

    /**
     * 推送 SSE 进度事件
     */
    private static void pushSseProgress(Long userId, String progressKey, Process process) {
        if (sseEventService != null && null != userId && StrUtil.isNotBlank(progressKey)) {
            try {
                sseEventService.sendProgress(String.valueOf(userId), progressKey, process);
            } catch (Exception e) {
                log.warn("推送 SSE 进度事件失败: userId={}, progressKey={}", userId, progressKey, e);
            }
        }
    }

    /**
     * 推送 SSE 完成事件
     */
    private static void pushSseComplete(Long userId, String progressKey, Process process) {
        if (sseEventService != null && null != userId && StrUtil.isNotBlank(progressKey)) {
            try {
                sseEventService.sendComplete(userId, progressKey, process);
            } catch (Exception e) {
                log.warn("推送 SSE 完成事件失败: userId={}, progressKey={}", userId, progressKey, e);
            }
        }
    }

    /**
     * 推送 SSE 错误事件（简单消息）
     * 内部会将字符串转换为 ProcessError 对象，确保前端接收的数据格式一致
     */
    public static void setProcessError(String key, String error, Long userId) {
        // 将字符串错误转换为 ProcessError 对象，保证数据格式一致
        ProcessError processError = new ProcessError("UNKNOWN_ERROR", error);
        setProcessError(key, processError, userId);
    }

    /**
     * 推送 SSE 错误事件（结构化错误信息）
     */
    public static void setProcessError(String key, ProcessError processError, Long userId) {
        if (sseEventService != null && null != userId && StrUtil.isNotBlank(key)) {
            try {
                sseEventService.sendError(String.valueOf(userId), key, processError);
            } catch (Exception e) {
                log.warn("推送 SSE 错误事件失败: userId={}, progressKey={}", userId, key, e);
            }
        }
    }

    /**
     * 推送 SSE 错误事件（构建器模式）
     */
    public static void setProcessError(String key, Long userId, String errorCode, String message) {
        ProcessError error = ProcessError.builder()
                .code(errorCode)
                .message(message)
                .build();
        setProcessError(key, error, userId);
    }

    @Data
    public static class Process {
        public int rate;
        public String url;
        public long current;
        public long total;
        public String tips;

        public Process(int rate, String url) {
            this.rate = rate;
            this.url = url;
        }

        public Process() {
        }


        public Process(long current, long total) {
            this.current = current;
            this.total = total;
            this.rate = total > 0 ?
                    NumberUtil.round(NumberUtil.div(current, total), 2).multiply(new BigDecimal(100)).intValue() : 0;
        }
    }

    /**
     * 结构化错误信息
     */
    public static class ProcessError {
        /** 错误代码 */
        public String code;
        /** 错误消息 */
        public String message;
        /** 错误详情 */
        public String details;
        /** 建议操作 */
        public String suggestion;
        /** 错误级别：ERROR, WARN, INFO */
        public String level = "ERROR";
        /** 是否可重试 */
        public boolean retryable = false;
        /** 额外数据 */
        public Object extra;

        public ProcessError() {}

        public ProcessError(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public ProcessError(String code, String message, String details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        public static ProcessErrorBuilder builder() {
            return new ProcessErrorBuilder();
        }
    }

    /**
     * 错误构建器
     */
    public static class ProcessErrorBuilder {
        private final ProcessError error = new ProcessError();

        public ProcessErrorBuilder code(String code) {
            error.code = code;
            return this;
        }

        public ProcessErrorBuilder message(String message) {
            error.message = message;
            return this;
        }

        public ProcessErrorBuilder details(String details) {
            error.details = details;
            return this;
        }

        public ProcessErrorBuilder suggestion(String suggestion) {
            error.suggestion = suggestion;
            return this;
        }

        public ProcessErrorBuilder level(String level) {
            error.level = level;
            return this;
        }

        public ProcessErrorBuilder retryable(boolean retryable) {
            error.retryable = retryable;
            return this;
        }

        public ProcessErrorBuilder extra(Object extra) {
            error.extra = extra;
            return this;
        }

        public ProcessError build() {
            return error;
        }
    }

}
