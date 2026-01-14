package com.ciyocloud.job.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务调度日志表 sys_job_log
 *
 * @author codeck
 */
@Data
@TableName("sys_job_log")
public class SysJobLogEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务组名
     */
    private String jobGroup;

    /**
     * 调用目标字符串
     */
    private String invokeTarget;

    /**
     * 日志信息
     */
    private String jobMessage;

    /**
     * 执行状态（0正常 1失败）
     */
    private Integer status;

    /**
     * 异常信息
     */
    private String exceptionInfo;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 停止时间
     */
    private LocalDateTime stopTime;


}
