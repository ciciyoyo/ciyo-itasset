package com.ciyocloud.common.sse.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SSE 事件数据模型
 *
 * @author codeck
 * @create 2026/01/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SseEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
    private String id;

    /**
     * 事件类型
     */
    private String event;

    /**
     * 事件数据
     */
    private Object data;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 进度标识
     */
    private String progressKey;

    /**
     * 重试次数
     */
    private Integer retry;

    /**
     * 事件时间
     */
    private LocalDateTime timestamp;

    /**
     * 构造进度推送事件
     */
    public static SseEvent progress(String userId, String progressKey, Object progressData) {
        SseEvent event = new SseEvent();
        event.setId(System.currentTimeMillis() + "");
        event.setEvent("progress");
        event.setData(progressData);
        event.setUserId(userId);
        event.setProgressKey(progressKey);
        event.setTimestamp(LocalDateTime.now());
        event.setRetry(3000);
        return event;
    }

    /**
     * 构造完成事件
     */
    public static SseEvent complete(String userId, String progressKey, Object result) {
        SseEvent event = new SseEvent();
        event.setId(System.currentTimeMillis() + "");
        event.setEvent("complete");
        event.setData(result);
        event.setUserId(userId);
        event.setProgressKey(progressKey);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    /**
     * 构造错误事件（字符串消息）
     */
    public static SseEvent error(String userId, String progressKey, String error) {
        SseEvent event = new SseEvent();
        event.setId(System.currentTimeMillis() + "");
        event.setEvent("error");
        event.setData(error);
        event.setUserId(userId);
        event.setProgressKey(progressKey);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    /**
     * 构造错误事件（结构化错误对象）
     */
    public static SseEvent error(String userId, String progressKey, Object errorData) {
        SseEvent event = new SseEvent();
        event.setId(System.currentTimeMillis() + "");
        event.setEvent("error");
        event.setData(errorData);
        event.setUserId(userId);
        event.setProgressKey(progressKey);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    /**
     * 转换为 SSE 格式字符串
     */
    public String toSseFormat() {
        StringBuilder sb = new StringBuilder();
        
        if (id != null) {
            sb.append("id: ").append(id).append("\n");
        }
        
        if (event != null) {
            sb.append("event: ").append(event).append("\n");
        }
        
        if (retry != null) {
            sb.append("retry: ").append(retry).append("\n");
        }
        
        if (data != null) {
            sb.append("data: ").append(data).append("\n");
        }
        
        sb.append("\n");
        return sb.toString();
    }
}
