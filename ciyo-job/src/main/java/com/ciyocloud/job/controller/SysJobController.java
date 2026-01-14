package com.ciyocloud.job.controller;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.job.constant.JobConstants;
import com.ciyocloud.job.entity.SysJobEntity;
import com.ciyocloud.job.service.SysJobService;
import com.ciyocloud.job.util.CronUtils;
import com.ciyocloud.job.util.ScheduleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 调度任务信息操作处理
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/job")
public class SysJobController {
    private final SysJobService jobService;

    /**
     * 查询定时任务列表
     */
    @PreAuthorize("@ss.hasPermi('system:job:list')")
    @GetMapping("/page")
    public Result<Page<SysJobEntity>> list(Page<SysJobEntity> page, SysJobEntity sysJob) {
        return Result.success(jobService.page(page, Wrappers.<SysJobEntity>lambdaQuery()
                .eq(StrUtil.isNotBlank(sysJob.getJobGroup()), SysJobEntity::getJobGroup, sysJob.getJobGroup())
                .eq(ObjUtil.isNotNull(sysJob.getStatus()), SysJobEntity::getStatus, sysJob.getStatus())
                .like(StrUtil.isNotBlank(sysJob.getJobName()), SysJobEntity::getJobName, sysJob.getJobName())));
    }


    /**
     * 获取定时任务详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:job:query')")
    @GetMapping(value = "/{jobId}")
    public Result<SysJobEntity> getInfo(@PathVariable("jobId") Long jobId) {
        return Result.success(jobService.getById(jobId));
    }

    /**
     * 新增定时任务
     */
    @PreAuthorize("@ss.hasPermi('system:job:add')")
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody SysJobEntity job) {
        if (!CronUtils.isValid(job.getCronExpression())) {
            return Result.failed("新增任务'" + job.getJobName() + "'失败，Cron表达式不正确");
        } else if (StrUtil.containsIgnoreCase(job.getInvokeTarget(), JobConstants.LOOKUP_RMI)) {
            return Result.failed("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'rmi'调用");
        } else if (StrUtil.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{JobConstants.LOOKUP_LDAP, JobConstants.LOOKUP_LDAPS})) {
            return Result.failed("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'ldap(s)'调用");
        } else if (StrUtil.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{JobConstants.HTTP, JobConstants.HTTPS})) {
            return Result.failed("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'http(s)'调用");
        } else if (StrUtil.containsAnyIgnoreCase(job.getInvokeTarget(), JobConstants.JOB_ERROR_STR)) {
            return Result.failed("新增任务'" + job.getJobName() + "'失败，目标字符串存在违规");
        } else if (!ScheduleUtils.whiteList(job.getInvokeTarget())) {
            return Result.failed("新增任务'" + job.getJobName() + "'失败，目标字符串不在白名单内");
        }
        job.setCreateBy(SecurityUtils.getUserId());
        return Result.success(jobService.saveJob(job));
    }

    /**
     * 修改定时任务
     */
    @PreAuthorize("@ss.hasPermi('system:job:update')")
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody SysJobEntity job) {
        if (!CronUtils.isValid(job.getCronExpression())) {
            return Result.failed("修改任务'" + job.getJobName() + "'失败，Cron表达式不正确");
        } else if (StrUtil.containsIgnoreCase(job.getInvokeTarget(), JobConstants.LOOKUP_RMI)) {
            return Result.failed("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'rmi'调用");
        } else if (StrUtil.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{JobConstants.LOOKUP_LDAP, JobConstants.LOOKUP_LDAPS})) {
            return Result.failed("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'ldap(s)'调用");
        } else if (StrUtil.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{JobConstants.HTTP, JobConstants.HTTPS})) {
            return Result.failed("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'http(s)'调用");
        } else if (StrUtil.containsAnyIgnoreCase(job.getInvokeTarget(), JobConstants.JOB_ERROR_STR)) {
            return Result.failed("修改任务'" + job.getJobName() + "'失败，目标字符串存在违规");
        } else if (!ScheduleUtils.whiteList(job.getInvokeTarget())) {
            return Result.failed("修改任务'" + job.getJobName() + "'失败，目标字符串不在白名单内");
        }
        job.setUpdateBy(SecurityUtils.getUserId());
        return Result.success(jobService.updateJob(job));
    }

    /**
     * 定时任务状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:job:changeStatus')")
    @PostMapping("/changeStatus")
    public Result<Boolean> changeStatus(@RequestBody SysJobEntity job) {
        SysJobEntity newJob = jobService.getById(job.getId());
        newJob.setStatus(job.getStatus());
        return Result.success(jobService.changeStatus(newJob));
    }

    /**
     * 定时任务立即执行一次
     */
    @PreAuthorize("@ss.hasPermi('system:job:changeStatus')")
    @PostMapping("/run")
    public Result<Boolean> run(@RequestBody SysJobEntity job) {
        boolean result = jobService.run(job);
        return result ? Result.success() : Result.failed("任务不存在或已过期！");
    }

    /**
     * 删除定时任务
     */
    @PreAuthorize("@ss.hasPermi('system:job:remove')")
    @PostMapping("/delete/{jobIds}")
    public Result<Boolean> remove(@PathVariable Long[] jobIds) {
        jobService.deleteJobByIds(jobIds);
        return Result.success();
    }
}
