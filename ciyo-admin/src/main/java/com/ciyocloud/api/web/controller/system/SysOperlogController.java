package com.ciyocloud.api.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import com.ciyocloud.system.entity.SysOperLogEntity;
import com.ciyocloud.system.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志记录
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/operlog")
public class SysOperlogController {
    private final SysOperLogService operLogService;


    /**
     * 获取操作日志列表
     *
     * @param page    分页
     * @param operLog 操作日志
     * @return 操作日志列表
     */
    @SaCheckPermission("monitor:operlog:list")
    @GetMapping("/page")
    public Result queryPage(Page page, SysOperLogEntity operLog) {
        return Result.success(operLogService.page(page, QueryWrapperUtils.toSimpleQuery(operLog)));
    }

    /**
     * 导出操作日志列表
     *
     * @param operLog 操作日志
     */
    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @SaCheckPermission("monitor:operlog:export")
    @GetMapping("/export")
    public void export(SysOperLogEntity operLog) {
        List<SysOperLogEntity> list = operLogService.list(QueryWrapperUtils.toSimpleQuery(operLog));
        ExcelUtils.exportExcel(list, "操作日志", SysOperLogEntity.class);
    }

    /**
     * 删除操作日志
     *
     * @param operIds 操作日志ID
     * @return 结果
     */
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @SaCheckPermission("monitor:operlog:remove")
    @DeleteMapping("/{operIds}")
    public Result remove(@PathVariable List<Long> operIds) {
        return Result.success(operLogService.removeByIds(operIds));
    }

    /**
     * 清空操作日志
     */
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @SaCheckPermission("monitor:operlog:remove")
    @DeleteMapping("/clean")
    public Result clean() {
        operLogService.cleanOperLog();
        return Result.success();
    }
}
