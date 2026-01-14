package com.ciyocloud.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.system.entity.SysOperLogEntity;

/**
 * 操作日志 服务层
 *
 * @author codeck
 */
public interface SysOperLogService extends IService<SysOperLogEntity> {


    /**
     * 清空操作日志
     */
    void cleanOperLog();
}
