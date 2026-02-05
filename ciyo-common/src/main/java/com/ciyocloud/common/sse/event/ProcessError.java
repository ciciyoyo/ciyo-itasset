package com.ciyocloud.common.sse.event;

/**
 * 进度错误信息 - 使用 JDK 18 Record 简化定义
 *
 * @param code       错误代码
 * @param message    错误消息
 * @param details    详细信息
 * @param suggestion 建议操作
 * @param level      错误级别（ERROR, WARN, INFO）
 * @param retryable  是否可重试
 * @param extra      扩展数据
 * @author codeck
 * @create 2026/01/31
 */
public record ProcessError(
        String code,
        String message,
        String details,
        String suggestion,
        String level,
        boolean retryable,
        Object extra
) {
    /**
     * 简单错误（仅 code 和 message）
     */
    public ProcessError(String code, String message) {
        this(code, message, null, null, "ERROR", false, null);
    }

    /**
     * 默认未知错误
     */
    public static ProcessError unknown(String message) {
        return new ProcessError("UNKNOWN_ERROR", message);
    }

    /**
     * 创建可重试的错误
     */
    public static ProcessError retryable(String code, String message, String suggestion) {
        return new ProcessError(code, message, null, suggestion, "ERROR", true, null);
    }

    /**
     * 创建警告级别错误
     */
    public static ProcessError warn(String code, String message) {
        return new ProcessError(code, message, null, null, "WARN", false, null);
    }

    /**
     * Builder 模式（保持兼容性）
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String code;
        private String message;
        private String details;
        private String suggestion;
        private String level = "ERROR";
        private boolean retryable = false;
        private Object extra;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public Builder suggestion(String suggestion) {
            this.suggestion = suggestion;
            return this;
        }

        public Builder level(String level) {
            this.level = level;
            return this;
        }

        public Builder retryable(boolean retryable) {
            this.retryable = retryable;
            return this;
        }

        public Builder extra(Object extra) {
            this.extra = extra;
            return this;
        }

        public ProcessError build() {
            return new ProcessError(code, message, details, suggestion, level, retryable, extra);
        }
    }
}
