package com.ciyocloud.job.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.job.constant.ScheduleConstants;
import com.ciyocloud.job.entity.SysJobEntity;
import com.ciyocloud.job.manager.JobScheduleManager;
import com.ciyocloud.job.mapper.SysJobMapper;
import com.ciyocloud.job.service.SysJobService;
import com.ciyocloud.job.util.CronUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 定时任务调度信息 服务层
 *
 * @author codeck
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJobEntity> implements SysJobService {

    private final JobScheduleManager jobScheduleManager;

    @PostConstruct
    public void init() {
        List<SysJobEntity> jobList = baseMapper.selectList(null);
        for (SysJobEntity job : jobList) {
            jobScheduleManager.scheduleJob(job);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pauseJob(SysJobEntity job) {
        job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
        boolean updated = this.updateById(job);
        if (updated) {
            jobScheduleManager.cancelJob(job.getId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resumeJob(SysJobEntity job) {
        job.setStatus(ScheduleConstants.Status.NORMAL.getValue());
        boolean updated = this.updateById(job);
        if (updated) {
            jobScheduleManager.scheduleJob(job);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJob(SysJobEntity job) {
        boolean updated = this.removeById(job);
        if (updated) {
            jobScheduleManager.cancelJob(job.getId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJobByIds(Long[] jobIds) {
        for (Long jobId : jobIds) {
            SysJobEntity job = this.getById(jobId);
            if (job != null) {
                deleteJob(job);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(SysJobEntity job) {
        boolean changed = true;
        Integer status = job.getStatus();
        if (ScheduleConstants.Status.NORMAL.getValue().equals(status)) {
            changed = resumeJob(job);
        } else if (ScheduleConstants.Status.PAUSE.getValue().equals(status)) {
            changed = pauseJob(job);
        }
        return changed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean run(SysJobEntity job) {
        // 立即运行一次
        jobScheduleManager.run(this.getById(job.getId()));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveJob(SysJobEntity job) {
        job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
        boolean success = this.save(job);
        // 新增时不自动启动，需手动开启
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJob(SysJobEntity job) {
        SysJobEntity properties = this.getById(job.getId());
        boolean updated = this.updateById(job);
        if (updated) {
            // 如果原来的状态是运行中，则更新任务调度
            if (ScheduleConstants.Status.NORMAL.getValue().equals(properties.getStatus())) {
                jobScheduleManager.scheduleJob(job);
            }
        }
        return updated;
    }

    @Override
    public boolean checkCronExpressionIsValid(String cronExpression) {
        return CronUtils.isValid(cronExpression);
    }
}
