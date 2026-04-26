package com.ciyocloud.common.sse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SSE 配置属性
 *
 * @author codeck
 * @create 2026/04/25
 */
@Data
@Component
@ConfigurationProperties(prefix = "ciyo.sse")
public class SseProperties {

    /**
     * 是否开启集群模式（默认关闭）
     * 开启后将使用 Redis 进行跨节点事件分发
     */
    private boolean clusterEnabled = false;

}
