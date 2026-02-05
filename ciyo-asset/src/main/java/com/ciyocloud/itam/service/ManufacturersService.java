package com.ciyocloud.itam.service;

import com.ciyocloud.common.mybatis.service.BaseService;
import com.ciyocloud.itam.entity.ManufacturersEntity;

import java.io.InputStream;

/**
 * 制造商Service接口
 *
 * @author codeck
 * @since 2025-12-29 15:58:20
 */
public interface ManufacturersService extends BaseService<ManufacturersEntity> {

    /**
     * 导入制造商数据
     *
     * @param inputStream      文件输入流
     * @param originalFilename 原始文件名
     * @param progressKey      前端传递的进度监听key
     * @param userId           用户 id
     */
    void importData(InputStream inputStream, String originalFilename, String progressKey, Long userId);

}
