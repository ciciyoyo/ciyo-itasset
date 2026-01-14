package com.ciyocloud.storage.cloud;


import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.constant.RedisKeyConstants;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.JsonUtils;
import com.ciyocloud.common.util.RedisUtils;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.storage.enums.OssTypeEnum;
import com.ciyocloud.storage.exception.InvalidExtensionException;
import com.ciyocloud.storage.exception.StorageException;
import com.ciyocloud.storage.util.MimeTypeUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

import static com.ciyocloud.common.constant.ConfigConstants.FILE_ENV_CONFIG;

/**
 * 文件上传Factory
 *
 * @author codeck
 */
@Slf4j
public final class OssStorageFactory {
    /**
     * 默认的文件名最大长度 100
     */
    public static final int DEFAULT_FILE_NAME_LENGTH = 100;
    private static final String CONFIG_KEY = StrUtil.format(RedisKeyConstants.ENV_CONFIG, FILE_ENV_CONFIG);
    private static OssStorageService storageService;
    @Getter
    private static OssStorageConfig config;

    static {
        build();
    }

    public static OssStorageService getStorageService() {
        if (null == storageService) {
            throw new BusinessException("未配置文件存储，请配置后使用");
        }
        return storageService;
    }

    public static synchronized void build() {
        OssStorageConfig config = JsonUtils.jsonToObj(SpringContextUtils.getBean(RedisUtils.class).get(CONFIG_KEY, String.class), OssStorageConfig.class);
        if (ObjectUtil.isNull(config)) {
            return;
        }
        if (config.getOssType() == OssTypeEnum.QINIU) {
            storageService = new QiniuCloudStorageService(config);
        } else if (config.getOssType() == OssTypeEnum.ALIYUN) {
            storageService = new AliyunOssStorageService(config);
        } else if (config.getOssType() == OssTypeEnum.LOCAL) {
            storageService = new LocalStorageService(config);
        } else if (config.getOssType() == OssTypeEnum.S3) {
            storageService = new S3StorageService(config);
        } else if (config.getOssType() == OssTypeEnum.TENCENT) {
            storageService = new QCloudOssStorageService(config);
        } else if (config.getOssType() == OssTypeEnum.MINIO) {
            storageService = new MinioStorageService(config);
        }
        log.info("更新存储配置完成:{}", JsonUtils.objToJson(config));
        OssStorageFactory.config = config;
    }


    /**
     * 校验文件格式是否正常
     */
    public static void checkAllowedExtension(MultipartFile file, String[] allowedExtension) {
        int fileNameLength = Objects.requireNonNull(file.getOriginalFilename()).length();
        if (fileNameLength > DEFAULT_FILE_NAME_LENGTH) {
            throw new StorageException("上传文件名称超出长度，最多允许100个字符");
        }
        String extension = FileNameUtil.extName(file.getOriginalFilename());
        if (StrUtil.isEmpty(extension)) {
            extension = MimeTypeUtils.getExtension(Objects.requireNonNull(file.getContentType()));
        }
        // 全部小写 避免有的文件后缀名大写
        if (StrUtil.isNotBlank(extension)) {
            extension = extension.toLowerCase();
        }
        String fileName = file.getOriginalFilename();
        if (allowedExtension != null && !ArrayUtil.contains(allowedExtension, extension)) {
            if (allowedExtension == MimeTypeUtils.IMAGE_EXTENSION) {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            } else if (allowedExtension == MimeTypeUtils.FLASH_EXTENSION) {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            } else if (allowedExtension == MimeTypeUtils.MEDIA_EXTENSION) {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            } else if (allowedExtension == MimeTypeUtils.VIDEO_EXTENSION) {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            } else {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            }
        }
    }

}
