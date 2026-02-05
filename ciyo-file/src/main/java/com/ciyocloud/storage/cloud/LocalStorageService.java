package com.ciyocloud.storage.cloud;


import cn.hutool.core.io.FileUtil;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.common.util.UrlUtils;
import lombok.extern.slf4j.Slf4j;

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
