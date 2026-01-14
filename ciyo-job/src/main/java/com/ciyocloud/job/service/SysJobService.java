package com.ciyocloud.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.job.entity.SysJobEntity;

/**
 * 定时任务调度信息信息 服务层
 *
 * @author codeck
 */
public interface SysJobService extends IService<SysJobEntity> {

    /**
     * 暂停任务
     *
     * @param job 调度信息
     * @return 结果
     */
    boolean pauseJob(SysJobEntity job);

    /**
     * 恢复任务
     *
     * @param job 调度信息
     * @return 结果
     */
    boolean resumeJob(SysJobEntity job);

    /**
     * 删除任务后，所对应的trigger也将被删除
     *
     * @param job 调度信息
     * @return 结果
     */
    boolean deleteJob(SysJobEntity job);

    /**
     * 批量删除调度信息
     *
     * @param jobIds 需要删除的任务ID
     * @return 结果
     */
    void deleteJobByIds(Long[] jobIds);

    /**
     * 任务调度状态修改
     *
     * @param job 调度信息
     * @return 结果
     */
    boolean changeStatus(SysJobEntity job);

    /**
     * 立即运行任务
     *
     * @param job 调度信息
     * @return 结果
     */
    boolean run(SysJobEntity job);

    /**
     * 新增任务
     *
     * @param job 调度信息
     * @return 结果
     */
    boolean saveJob(SysJobEntity job);

    /**
     * 更新任务
     *
     * @param job 调度信息
     * @return 结果
     */
    boolean updateJob(SysJobEntity job);

    /**
     * 校验cron表达式是否有效
     *
     * @param cronExpression 表达式
     * @return 结果
     */
    boolean checkCronExpressionIsValid(String cronExpression);
}
