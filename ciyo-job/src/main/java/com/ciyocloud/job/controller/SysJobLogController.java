package com.ciyocloud.job.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.job.entity.SysJobLogEntity;
import com.ciyocloud.job.service.SysJobLogService;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 调度日志操作处理
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/jobLog")
public class SysJobLogController {
    private final SysJobLogService jobLogService;

    /**
     * 查询定时任务调度日志列表
     */
    @SaCheckPermission("system:job:list")
    @GetMapping("/page")
    public Result<Page<SysJobLogEntity>> page(Page<SysJobLogEntity> page, SysJobLogEntity sysJobLog) {
        return Result.success(jobLogService.page(page, QueryWrapperUtils.toSimpleQuery(sysJobLog)));
    }


    /**
     * 根据调度编号获取详细信息
     */
    @SaCheckPermission("system:job:query")
    @GetMapping(value = "/{jobLogId}")
    public Result<SysJobLogEntity> getInfo(@PathVariable Long jobLogId) {
        return Result.success(jobLogService.getById(jobLogId));
    }


    /**
     * 删除定时任务调度日志
     */
    @SaCheckPermission("system:job:remove")
    @PostMapping("/delete/{jobLogIds}")
    public Result<Boolean> remove(@PathVariable List<Long> jobLogIds) {
        return Result.success(jobLogService.removeBatchByIds(jobLogIds));
    }

    /**
     * 清空定时任务调度日志
     */
    @SaCheckPermission("system:job:remove")
    @PostMapping("/clean")
    public Result<Void> clean() {
        jobLogService.cleanAll();
        return Result.success();
    }
}
