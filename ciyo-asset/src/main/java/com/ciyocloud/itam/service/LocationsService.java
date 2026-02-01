package com.ciyocloud.itam.service;

import com.ciyocloud.common.mybatis.service.BaseService;
import com.ciyocloud.itam.entity.LocationsEntity;

import java.io.InputStream;
import java.util.List;

/**
 * 物理位置Service接口
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
public interface LocationsService extends BaseService<LocationsEntity> {


    /**
     * 获取物理位置树
     *
     * @return 树形结构的物理位置列表
     */
    List<LocationsEntity> getTree(LocationsEntity locations);
    
    /**
     * 导入物理位置数据
     *
     * @param inputStream      文件输入流
     * @param originalFilename 原始文件名
     * @param progressKey      前端传递的进度监听key
     * @param userId           用户 id
     */
    void importData(InputStream inputStream, String originalFilename, String progressKey, Long userId);
}
