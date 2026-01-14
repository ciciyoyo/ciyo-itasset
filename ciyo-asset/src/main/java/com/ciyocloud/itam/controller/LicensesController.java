package com.ciyocloud.itam.controller;

import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.LicensesEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.req.LicenseAllocationReq;
import com.ciyocloud.itam.req.LicensePageReq;
import com.ciyocloud.itam.service.AssetsReportService;
import com.ciyocloud.itam.service.LicensesService;
import com.ciyocloud.itam.vo.AssetsReportVO;
import com.ciyocloud.itam.vo.LicensesVO;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 软件授权
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/licenses")
public class LicensesController {

    private final LicensesService licensesService;
    private final AssetsReportService assetsReportService;

    /**
     * 查询软件授权列表
     */
    @PreAuthorize("@ss.hasPermi('itam:licenses:page')")
    @GetMapping("/page")
    public Result<PageResultVO<LicensesVO>> queryPage(PageRequest page, LicensePageReq req) {
        return Result.success(new PageResultVO<>(licensesService.queryPageVo(page.toMpPage(), req)));
    }

    /**
     * 导出软件授权列表
     */
    @PreAuthorize("@ss.hasPermi('itam:licenses:export')")
    @Log(title = "软件授权", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(LicensePageReq req) {
        List<LicensesVO> list = licensesService.queryListVo(req);
        ExcelUtils.exportExcel(list, "软件授权数据", LicensesVO.class);
    }

    /**
     * 获取软件授权详细信息
     *
     * @param id 主键
     */
    @PreAuthorize("@ss.hasPermi('itam:licenses:query')")
    @GetMapping(value = "/{id}")
    public Result<LicensesEntity> getInfo(@PathVariable("id") Long id) {
        return Result.success(licensesService.getById(id));
    }

    /**
     * 新增软件授权
     */
    @PreAuthorize("@ss.hasPermi('itam:licenses:add')")
    @Log(title = "软件授权", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result<Boolean> add(@Validated(AddGroup.class) @RequestBody LicensesEntity licenses) {
        ValidatorUtils.validateEntity(licenses, AddGroup.class);
        return Result.success(licensesService.save(licenses));
    }

    /**
     * 修改软件授权
     */
    @PreAuthorize("@ss.hasPermi('itam:licenses:update')")
    @Log(title = "软件授权", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody LicensesEntity licenses) {
        ValidatorUtils.validateEntity(licenses, UpdateGroup.class);
        return Result.success(licensesService.updateById(licenses));
    }

    /**
     * 删除软件授权
     *
     * @param ids 主键串
     */
    @PreAuthorize("@ss.hasPermi('itam:licenses:delete')")
    @Log(title = "软件授权", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(licensesService.removeLicensesByIds(ids));
    }

    /**
     * 分配软件授权
     */
    @PreAuthorize("@ss.hasPermi('itam:licenses:allocate')")
    @Log(title = "软件授权", businessType = BusinessType.INSERT)
    @PostMapping("/allocate")
    public Result<Boolean> allocate(@RequestBody LicenseAllocationReq req) {
        return Result.success(licensesService.allocate(req));
    }

    /**
     * 解除授权到设备
     */
    @PreAuthorize("@ss.hasPermi('itam:licenses:allocate')")
    @Log(title = "软件授权", businessType = BusinessType.UPDATE)
    @PostMapping("/deallocate/{allocationId}")
    public Result<Boolean> deallocate(@PathVariable Long allocationId) {
        return Result.success(licensesService.deallocate(allocationId));
    }

    /**
     * 获取每月授权价值统计
     */
    @PreAuthorize("@ss.hasPermi('itam:licenses:query')")
    @GetMapping("/stats/monthly-value")
    public Result<List<AssetsReportVO>> getMonthlyValueStats() {
        return Result.success(assetsReportService.getMonthlyTrend(AssetType.LICENSE));
    }

    /**
     * 获取授权分类统计
     */
    @PreAuthorize("@ss.hasPermi('itam:licenses:query')")
    @GetMapping("/stats/category")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        return Result.success(licensesService.getCategoryStats());
    }

    /**
     * 获取常用指标统计
     */
    @PreAuthorize("@ss.hasPermi('itam:licenses:query')")
    @GetMapping("/stats/indicators")
    public Result<Map<String, Object>> getIndicatorStats() {
        return Result.success(licensesService.getIndicatorStats());
    }
}
