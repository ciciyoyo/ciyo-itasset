package com.ciyocloud.common.constant;

/**
 * @description: 通用的常亮
 * @author: smalljop
 * @create: 2018-09-27 13:25
 **/
public interface CommonConstants {


    /**
     * 超级管理员id
     */
    Long SUPER_ADMIN_ID = 1L;
    /**
     * 用户正常状态
     * 1:正常 0 :禁用
     */
    Integer USER_NORMAL_STATUS = 1;
    /**
     * 文件下载content_type
     */
    String FILE_DOWNLOAD_CONTENT_TYPE = "application/octet-stream;charset=UTF-8";


    /**
     * 跳转url
     */
    String JUMP_URL_KEY = "jumpUrl";


    /**
     * 系统版本
     */
    String SYSTEM_VERSION = "0.1";

}
