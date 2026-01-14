package com.ciyocloud.storage.cloud;

import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.storage.exception.StorageException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * minio 存储
 *
 * @author : smalljop
 * @since :  2024-09-05
 **/
@Slf4j
public class MinioStorageService extends OssStorageService {

    MinioClient minioClient;

    MinioStorageService(OssStorageConfig config) {
        this.config = config;
        try {
            this.minioClient = MinioClient.builder().endpoint(config.getEndpoint()).credentials(config.getAccessKeyId(), config.getAccessKeySecret()).build();

        } catch (Exception e) {
            throw new StorageException("配置错误! 请检查系统配置:[" + e.getMessage() + "]");
        }
    }


    @Override
    public String upload(InputStream inputStream, String path) {
        return uploadStream(inputStream, path);
    }

    @Override
    public String upload(byte[] data, String path) {
        return uploadStream(new ByteArrayInputStream(data), path);
    }

    public String uploadStream(InputStream inputStream, String path) {
        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(config.getBucketName()).object(path).stream(inputStream, inputStream.available(), -1).build());
            inputStream.close();
            return getUrl(path);
        } catch (Exception e) {
            throw new BusinessException("上传文件失败" + e.getMessage());
        }
    }


    @SneakyThrows
    @Override
    public void delete(String path) {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(config.getBucketName())
                .object(path)
                .build());
    }
}
