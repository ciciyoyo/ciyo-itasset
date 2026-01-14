package com.ciyocloud.itam.controller;

import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.FailuresEntity;
import com.ciyocloud.itam.entity.OfferingEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.enums.OfferingStatus;
import com.ciyocloud.itam.req.OfferingPageReq;
import com.ciyocloud.itam.service.AssetsReportService;
import com.ciyocloud.itam.service.FailuresService;
import com.ciyocloud.itam.service.OfferingService;
import com.ciyocloud.itam.vo.AssetsReportVO;
import com.ciyocloud.itam.vo.OfferingVO;
import com.ciyocloud.itam.vo.SupplierFailureStatsVO;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 服务
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/offering")
public class OfferingController {

    private final OfferingService offeringService;
    private final FailuresService failuresService;
    private final AssetsReportService assetsReportService;


    /**
     * 查询服务列表 (VO)
     */
    @SaCheckPermission("itam:offering:page")
    @GetMapping("/page")
    public Result<PageResultVO<OfferingVO>> queryPageVo(PageRequest page, OfferingPageReq req) {
        return Result.success(new PageResultVO<>(offeringService.queryPageVo(page.toMpPage(), req)));
    }

    /**
     * 导出服务列表
     */
    @SaCheckPermission("itam:offering:export")
    @Log(title = "服务", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(OfferingPageReq req) {
        List<OfferingVO> list = offeringService.queryListVo(req);
        ExcelUtils.exportExcel(list, "服务数据", OfferingVO.class);
    }

    /**
     * 获取服务详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("itam:offering:query")
    @GetMapping(value = "/{id}")
    public Result<OfferingVO> getInfo(@PathVariable("id") Long id) {
        return Result.success(offeringService.getOfferingDetail(id));
    }

    /**
     * 新增服务
     */
    @SaCheckPermission("itam:offering:add")
    @Log(title = "服务", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result<Boolean> add(@Validated(AddGroup.class) @RequestBody OfferingEntity offering) {
        offering.setOfferingStatus(OfferingStatus.NORMAL);
        ValidatorUtils.validateEntity(offering, AddGroup.class);
        return Result.success(offeringService.save(offering));
    }

    /**
     * 修改服务
     */
    @SaCheckPermission("itam:offering:update")
    @Log(title = "服务", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody OfferingEntity offering) {
        ValidatorUtils.validateEntity(offering, UpdateGroup.class);
        return Result.success(offeringService.updateById(offering));
    }

    /**
     * 删除服务
     *
     * @param ids 主键串
     */
    @SaCheckPermission("itam:offering:delete")
    @Log(title = "服务", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(offeringService.removeOfferingsByIds(ids));
    }

    /**
     * 服务归属到设备
     */
    @SaCheckPermission("itam:offering:update")
    @Log(title = "服务", businessType = BusinessType.UPDATE)
    @PostMapping("/bind-asset")
    public Result<Boolean> bindAsset(@RequestBody OfferingEntity offering) {
        if (offering.getId() == null || offering.getTargetId() == null) {
            return Result.failed("服务ID和设备ID不能为空");
        }
        offeringService.bindAsset(offering.getId(), offering.getTargetId());
        return Result.success(true);
    }

    /**
     * 解除服务归属
     */
    @SaCheckPermission("itam:offering:update")
    @Log(title = "服务", businessType = BusinessType.UPDATE)
    @PostMapping("/unbind")
    public Result<Boolean> unbind(@RequestBody OfferingEntity offering) {
        if (offering.getId() == null) {
            return Result.failed("服务ID不能为空");
        }
        offeringService.unbind(offering.getId());
        return Result.success(true);
    }

    /**
     * 报告故障
     */
    @SaCheckPermission("itam:offering:update")
    @Log(title = "故障", businessType = BusinessType.INSERT)
    @PostMapping("/report-exception")
    public Result<Boolean> report(@RequestBody FailuresEntity failures) {
        failuresService.reportFailure(failures);
        return Result.success(true);
    }

    /**
     * 解决故障
     */
    @SaCheckPermission("itam:offering:update")
    @Log(title = "故障", businessType = BusinessType.UPDATE)
    @PostMapping("/resolve-exception")
    public Result<Boolean> resolve(@RequestBody FailuresEntity failures) {
        failuresService.resolveFailure(failures.getId(), failures.getNotes());
        return Result.success(true);
    }


    /**
     * 获取服务常用指标统计
     */
    @SaCheckPermission("itam:offering:page")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getOfferingStatistics() {
        return Result.success(offeringService.getOfferingStatistics());
    }

    /**
     * 获取每月服务价值统计
     *
     * @param year 年份
     */
    @SaCheckPermission("itam:offering:page")
    @GetMapping("/statistics/service-value")
    public Result<List<AssetsReportVO>> getServiceValueStats(@RequestParam(required = false) Integer year) {
        return Result.success(assetsReportService.getMonthlyTrend(AssetType.SERVICE));
    }


    /**
     * 统计不同服务商提供的服务出现异常的数量
     */
    @SaCheckPermission("itam:offering:page")
    @GetMapping("/statistics/supplier-failure-stats")
    public Result<List<SupplierFailureStatsVO>> getSupplierFailureStats() {
        return Result.success(assetsReportService.getSupplierFailureStats());
    }
}
