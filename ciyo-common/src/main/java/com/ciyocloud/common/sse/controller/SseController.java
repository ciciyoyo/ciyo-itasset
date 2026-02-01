package com.ciyocloud.common.sse.controller;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.sse.constants.SseConstants;
import com.ciyocloud.common.sse.context.SseContext;
import com.ciyocloud.common.sse.manager.SseConnectionManager;
import com.ciyocloud.common.sse.service.SseEventService;
import com.ciyocloud.common.sse.util.SseAsyncProcessUtils;
import com.ciyocloud.common.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * SSE 控制器
 * 每个用户每个平台只建立一个连接，通过 progressKey 区分不同的业务
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
     * 平台标识从请求头 X-Platform 获取，默认为 web
     */
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(HttpServletRequest request) {
        String platform = SseConstants.DEFAULT_PLATFORM;
        try {
            String userId = SecurityUtils.getUserId().toString();
            platform = request.getHeader(SseConstants.PLATFORM_HEADER);
            if (StrUtil.isBlank(platform)) {
                platform = SseConstants.DEFAULT_PLATFORM;
            }

            log.info("建立 SSE 连接: userId={}, platform={}", userId, platform);
            return connectionManager.createConnection(userId, platform);
        } catch (Exception e) {
            log.error("创建 SSE 连接失败: platform={}", platform, e);
        }
        return null;
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
                           @RequestParam String platform,
                           @RequestParam String progressKey,
                           @RequestParam String message) {
        try {
            ThreadUtil.execute(() -> {
                SseContext.execute(Long.parseLong(userId), progressKey, platform, () -> {
                    SseAsyncProcessUtils.setTips("正在解析Excel文件...");
                    SseAsyncProcessUtils.setProcess(50, message);
                });
            });
            return "发送成功";
        } catch (Exception e) {
            log.error("发送测试消息失败", e);
            return "发送失败: " + e.getMessage();
        }
    }

    /**
     * 关闭指定用户平台连接
     */
    @PostMapping("/close")
    public String closeConnection(@RequestParam String userId,
                                  @RequestParam String platform) {
        try {
            sseEventService.closeConnection(userId, platform);
            return "连接关闭成功";
        } catch (Exception e) {
            log.error("关闭连接失败", e);
            return "关闭连接失败: " + e.getMessage();
        }
    }
}
