package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.itam.entity.LocationsEntity;

import java.util.List;

/**
 * 物理位置Service接口
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
public interface LocationsService extends IService<LocationsEntity> {


    /**
     * 获取物理位置树
     *
     * @return 树形结构的物理位置列表
     */
    List<LocationsEntity> getTree(LocationsEntity locations);
}
