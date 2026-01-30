package com.ciyocloud.common.sse.controller;

import cn.hutool.core.thread.ThreadUtil;
import com.ciyocloud.common.sse.manager.SseConnectionManager;
import com.ciyocloud.common.sse.service.SseEventService;
import com.ciyocloud.common.sse.util.SseAsyncProcessUtils;
import com.ciyocloud.common.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * SSE 控制器
 * 提供 SSE 连接端点和管理接口
 *
 * @author codeck
 * @create 2026/01/28
 */
@Slf4j
@RestController
@RequestMapping("/sse")
public class SseController {

    @Autowired
    private SseConnectionManager connectionManager;

    @Autowired
    private SseEventService sseEventService;

    /**
     * 建立 SSE 连接
     * 用于监听进度更新
     */
    @GetMapping(value = "/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam String progressKey) {
        try {
            // 获取当前用户ID（根据实际认证体系调整）
            String userId = SecurityUtils.getUserId().toString();

            log.info("建立 SSE 连接: userId={}, progressKey={}",
                userId, progressKey);

            return connectionManager.createConnection(userId, progressKey);

        } catch (Exception e) {
            log.error("创建 SSE 连接失败: progressKey={}", progressKey, e);
            throw new RuntimeException("SSE 连接创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取连接统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return sseEventService.getConnectionStats();
    }

    /**
     * 手动推送测试消息
     */
    @PostMapping("/test/send")
    public String testSend(@RequestParam String userId,
                          @RequestParam String progressKey,
                          @RequestParam String message) {
        try {
            ThreadUtil.execute(() -> {
                SseAsyncProcessUtils.setProcessTips(progressKey, "正在解析Excel文件...", Long.parseLong(userId));

                sseEventService.sendProgress(userId, progressKey, Map.of("test", message));
            });
            return "发送成功";
        } catch (Exception e) {
            log.error("发送测试消息失败", e);
            return "发送失败: " + e.getMessage();
        }
    }

    /**
     * 关闭指定用户连接
     */
    @PostMapping("/close")
    public String closeConnection(@RequestParam String userId,
                                 @RequestParam String progressKey) {
        try {
            sseEventService.closeConnection(userId, progressKey);
            return "连接关闭成功";
        } catch (Exception e) {
            log.error("关闭连接失败", e);
            return "关闭连接失败: " + e.getMessage();
        }
    }
}
