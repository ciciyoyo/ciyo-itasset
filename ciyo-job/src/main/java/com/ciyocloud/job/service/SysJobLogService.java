package com.ciyocloud.job.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.job.entity.SysJobLogEntity;

/**
 * 定时任务调度日志信息信息 服务层
 *
 * @author codeck
 */
public interface SysJobLogService extends IService<SysJobLogEntity> {

    /**
     * 清除6个月前的日志记录
     */
    void cleanAll();
}
