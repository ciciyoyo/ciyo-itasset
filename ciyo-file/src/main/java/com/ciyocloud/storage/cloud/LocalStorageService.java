package com.ciyocloud.storage.cloud;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.common.util.UrlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.File;
import java.io.InputStream;

/**
 * 本地文件存储
 *
 * @author : codeck
 * @since : 2018-10-18 14:01
 */
@Slf4j
public class LocalStorageService extends OssStorageService {

    private final ExtraStorageProperties extraStorageProperties;

    public LocalStorageService(OssStorageConfig config) {
        this.config = config;
        extraStorageProperties = SpringContextUtils.getBean(ExtraStorageProperties.class);

        // 初始化默认配置
        initializeDefaultConfig();
    }

    /**
     * 初始化默认配置
     * 如果路径和域名未配置，使用默认值
     */
    private void initializeDefaultConfig() {
        // 如果上传文件夹为空，使用当前运行目录
        if (StrUtil.isBlank(config.getUploadFolder())) {
            String defaultUploadFolder = System.getProperty("user.dir") + File.separator + "upload";
            config.setUploadFolder(defaultUploadFolder);
            log.info("使用默认上传文件夹: {}", defaultUploadFolder);
        }

        // 如果域名为空，尝试从当前请求中获取
        if (StrUtil.isBlank(config.getDomain())) {
            String defaultDomain = getDefaultDomain();
            if (StrUtil.isNotBlank(defaultDomain)) {
                config.setDomain(defaultDomain);
                log.info("使用默认域名: {}", defaultDomain);
            } else {
                // 如果无法获取请求上下文，使用本地地址
                config.setDomain("http://localhost:8080");
                log.warn("无法获取请求域名，使用默认值: http://localhost:8080");
            }
        }
    }

    /**
     * 获取当前请求的默认域名
     *
     * @return 域名 (如: http://localhost:8080 或 https://example.com)
     */
    private String getDefaultDomain() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String scheme = request.getScheme(); // http 或 https
                String serverName = request.getServerName(); // 服务器地址
                int serverPort = request.getServerPort(); // 服务器端口

                // 构建域名
                String domain = scheme + "://" + serverName;

                // 如果不是默认端口，需要拼接
                if ((scheme.equals("http") && serverPort != 80) ||
                    (scheme.equals("https") && serverPort != 443)) {
                    domain += ":" + serverPort;
                }

                return domain;
            }
        } catch (Exception e) {
            log.debug("获取请求上下文失败: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public String upload(InputStream inputStream, String path) {
        File file = FileUtil.file(config.getUploadFolder() + File.separator + path);
        //目录是否存在
        if (!FileUtil.exist(file.getParent())) {
            FileUtil.mkdir(file.getParent());
        }
        log.info("文件上传路径：{}", file.getAbsolutePath());
        FileUtil.writeFromStream(inputStream, file);
        return getUploadUrl(path);
    }

    @Override
    public String upload(byte[] data, String path) {
        File file = FileUtil.file(config.getUploadFolder() + File.separator + path);
        //目录是否存在
        if (!FileUtil.exist(file.getParent())) {
            FileUtil.mkdir(file.getParent());
        }
        FileUtil.writeBytes(data, file);
        return getUploadUrl(path);
    }


    public String getUploadUrl(String path) {
        if (ExtraStorageProperties.PathModeEnum.RELATIVE.getCode().equals(extraStorageProperties.getPathMode())) {
            return UrlUtils.joinUrl(extraStorageProperties.getRelativePathPrefix(), path);
        } else {
            return getUrl(path);
        }
    }

    @Override
    public void delete(String path) {
        FileUtil.del(config.getUploadFolder() + File.separator + path);
    }
}
