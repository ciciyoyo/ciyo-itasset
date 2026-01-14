package com.ciyocloud.job.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.job.entity.SysJobLogEntity;
import com.ciyocloud.job.mapper.SysJobLogMapper;
import com.ciyocloud.job.service.SysJobLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


/**
 * 定时任务调度日志信息 服务层
 *
 * @author codeck
 */
@Service
public class SysJobLogServiceImpl extends ServiceImpl<SysJobLogMapper, SysJobLogEntity> implements SysJobLogService {

    @Override
    public void cleanAll() {
        baseMapper.cleanJobLog(LocalDateTime.now().minusMonths(6));
    }
}
