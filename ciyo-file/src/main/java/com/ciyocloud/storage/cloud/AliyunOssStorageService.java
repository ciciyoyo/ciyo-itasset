package com.ciyocloud.storage.cloud;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.ciyocloud.storage.exception.StorageException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 阿里云oss
 *
 * @author : codeck
 * @since : 2018-10-18 14:01
 **/
public class AliyunOssStorageService extends OssStorageService {

    private OSS client;

    public AliyunOssStorageService(OssStorageConfig config) {
        this.config = config;
        //初始化
        init();
    }

    private void init() {
        client = new OSSClientBuilder()
                .build(config.getEndpoint(), config.getAccessKeyId(), config.getAccessKeySecret());
    }


    @Override
    public String upload(InputStream inputStream, String path) {
        try {
            // 阿里云前面不能带/ 如果有就移除掉
            String newPath = fullPath(path);
            if (newPath.startsWith("/")) {
                newPath = newPath.substring(1);
            }
            client.putObject(config.getBucketName(), newPath, inputStream);
        } catch (Exception e) {
            throw new StorageException("上传文件失败，请检查配置信息", e);
        }
        return getUrl(path);
    }

    @Override
    public String upload(byte[] data, String path) {
        return upload(new ByteArrayInputStream(data), path);
    }


//    @Override
//    public InputStream download(String path) {
//        // ossObject包含文件所在的存储空间名称、文件名称、文件元信息以及一个输入流。
//        OSSObject ossObject = client.getObject(com.codeck.manage.platform.com.ciyocloud.wx.mp.config.getBucketName(), path);
//        ByteOutputStream out = new ByteOutputStream();
//        return ossObject.getObjectContent();
//    }

    @Override
    public void delete(String path) {
        client.deleteObject(config.getBucketName(), path);
    }
}
