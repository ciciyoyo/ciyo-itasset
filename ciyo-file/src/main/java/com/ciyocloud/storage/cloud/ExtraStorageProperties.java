package com.ciyocloud.storage.cloud;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 * 扩展存储配置
 *
 * @author : codeck
 * @since :  2025/10/24 18:57
 **/
@Configuration
@ConfigurationProperties(prefix = "platform.form-file")
@Data
public class ExtraStorageProperties {

    /**
     * 路径模式（absolute | relative）
     * 在文件存储在本地时可用  开启了相对路径之后文件存储配置的域名只在导出有用 默认区当前访问的域名 域名要包含relativePathPrefix
     */
    private String pathMode = "absolute";

    /**
     * 相对路径前缀
     * 路径模式是 relative 时有效
     */
    private String relativePathPrefix = "/u/";

    /**
     * 下载模式（common | local）
     * 表单文件下载模式，默认通用模式 common
     */
    private String downloadMode = "common";

    /**
     * 文件路径模式
     */
    @Getter
    @AllArgsConstructor
    public enum PathModeEnum {
        ABSOLUTE("absolute", "绝对路径"),
        RELATIVE("relative", "相对路径");

        private final String code;
        private final String desc;
    }

    /**
     * 文件下载模式
     */
    @Getter
    @AllArgsConstructor
    public enum DownloadModeEnum {
        COMMON("common", "通用模式"),
        LOCAL("local", "本地模式");

        private final String code;
        private final String desc;
    }
}
