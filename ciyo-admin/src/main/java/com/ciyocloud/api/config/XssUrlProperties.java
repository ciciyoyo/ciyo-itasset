package com.ciyocloud.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "xss")
@Data
public class XssUrlProperties {
    /**
     * 排除路径
     */
    private String excludes;

    /**
     * 需要处理的路径
     */
    private String urlPatterns;

    /**
     * 允许保存富文本的
     * 富文本只保存特定的标签
     */
    private List<String> richTextUrls;

}
