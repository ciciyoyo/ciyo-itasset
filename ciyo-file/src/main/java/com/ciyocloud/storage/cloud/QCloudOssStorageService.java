package com.ciyocloud.storage.cloud;

import com.ciyocloud.storage.exception.StorageException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 腾讯文
 *
 * @author : codeck
 * @since :  2023/2/2
 **/
public class QCloudOssStorageService extends OssStorageService {

    private COSClient client;

    public QCloudOssStorageService(OssStorageConfig config) {
        // https://console.cloud.tencent.com/cam/capi
        this.config = config;
        //初始化
        init();
    }

    private void init() {
        COSCredentials cred = new BasicCOSCredentials(config.getAccessKeyId(),
                config.getAccessKeySecret());
        // 2 设置 bucket 的地域, COS 地域的简称请参见 https://cloud.tencent.com/document/product/436/6224
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        Region region = new Region(config.getEndpoint());
        ClientConfig clientConfig = new ClientConfig(region);
        // 这里建议设置使用 https 协议
        // 从 5.6.54 版本开始，默认使用了 https
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 3 生成 cos 客户端。
        client = new COSClient(cred, clientConfig);
    }


    @Override
    public String upload(InputStream inputStream, String path) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            PutObjectRequest putObjectRequest = new PutObjectRequest(config.getBucketName(), path, inputStream, metadata);
            client.putObject(putObjectRequest);
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
