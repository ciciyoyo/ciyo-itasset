package com.ciyocloud.storage.cloud;

import com.ciyocloud.storage.enums.OssTypeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Oss配置
 *
 * @author : codeck
 * @since : 2018-10-18 13:53
 **/
@Data
@Slf4j
public class OssStorageConfig {


    /**
     * oss 类型
     * 参考 OssTypeEnum.java
     */
    private OssTypeEnum ossType;


    /**
     * 阿里云：endpoint
     */
    private String endpoint;


    /**
     * accessKeyId
     */
    private String accessKeyId;

    /**
     * accessKeySecret
     */
    private String accessKeySecret;


    /**
     * 桶名
     */
    private String bucketName;


    /**
     * 预览域名
     */
    private String domain;

    /**
     * oss 前缀目录
     */
    private String prefixPath;

    /**
     * 本地存储文件存放地址
     */
    private String uploadFolder;


    /**
     * region 名称
     */
    private String regionName;


    /**
     * 权限控制 acl
     */
    private String acl;
    /**
     * 本地存储文件访问路径
     */
    private String accessPathPattern = "/u/**";


}
