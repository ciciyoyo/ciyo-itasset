package com.ciyocloud.api.web.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.ciyocloud.common.constant.CommonConstants.SYSTEM_VERSION;

/**
 * 健康检查接口
 *
 * @author codeck
 */
@RestController
public class HealthController {

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 健康检查接口
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    @SaIgnore
    public Map<String, Object> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", Instant.now().toString());
        status.put("service", applicationName);
        status.put("version", SYSTEM_VERSION);
        return status;
    }
}
