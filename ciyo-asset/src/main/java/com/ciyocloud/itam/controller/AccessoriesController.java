package com.ciyocloud.itam.controller;

import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.AccessoriesEntity;
import com.ciyocloud.itam.entity.AllocationsEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.req.AccessoriesPageReq;
import com.ciyocloud.itam.req.AllocationsPageReq;
import com.ciyocloud.itam.req.FailuresReportReq;
import com.ciyocloud.itam.service.AccessoriesService;
import com.ciyocloud.itam.service.AllocationsService;
import com.ciyocloud.itam.service.AssetsReportService;
import com.ciyocloud.itam.service.FailuresService;
import com.ciyocloud.itam.vo.AccessoriesVO;
import com.ciyocloud.itam.vo.AllocationsVO;
import com.ciyocloud.itam.vo.AssetsReportVO;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 配件
 *
 * @author codeck
 * @since 2025-12-29 20:10:26
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/accessories")
public class AccessoriesController {

    private final AccessoriesService accessoriesService;
    private final AllocationsService allocationsService;
    private final FailuresService failuresService;
    private final AssetsReportService assetsReportService;

    /**
     * 查询配件列表
     */
    @SaCheckPermission("itam:accessories:page")
    @GetMapping("/page")
    public Result<PageResultVO<AccessoriesVO>> queryPage(PageRequest page, AccessoriesPageReq req) {
        return Result.success(new PageResultVO<>(accessoriesService.queryPageVo(page.toMpPage(), QueryWrapperUtils.toSimpleQuery(req, AccessoriesEntity.class))));
    }

    /**
     * 查询配件列表
     */
    @SaCheckPermission("itam:accessories:list")
    @GetMapping("/list")
    public Result<List<AccessoriesVO>> list(AccessoriesPageReq req) {
        return Result.success(accessoriesService.queryListVo(QueryWrapperUtils.toSimpleQuery(req, AccessoriesEntity.class)));
    }

    /**
     * 导出配件列表
     */
    @SaCheckPermission("itam:accessories:export")
    @Log(title = "配件", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(AccessoriesPageReq req) {
        List<AccessoriesVO> list = accessoriesService.queryListVo(QueryWrapperUtils.toSimpleQuery(req, AccessoriesEntity.class));
        ExcelUtils.exportExcel(list, "配件数据", AccessoriesVO.class);
    }

    /**
     * 获取配件详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("itam:accessories:query")
    @GetMapping(value = "/{id:\\d+}")
    public Result<AccessoriesEntity> getInfo(@PathVariable("id") Long id) {
        return Result.success(accessoriesService.getById(id));
    }

    /**
     * 新增配件
     */
    @SaCheckPermission("itam:accessories:add")
    @Log(title = "配件", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result<Boolean> add(@Validated(AddGroup.class) @RequestBody AccessoriesEntity accessories) {
        ValidatorUtils.validateEntity(accessories, AddGroup.class);
        return Result.success(accessoriesService.save(accessories));
    }

    /**
     * 修改配件
     */
    @SaCheckPermission("itam:accessories:update")
    @Log(title = "配件", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody AccessoriesEntity accessories) {
        ValidatorUtils.validateEntity(accessories, UpdateGroup.class);
        return Result.success(accessoriesService.updateById(accessories));
    }

    /**
     * 删除配件
     *
     * @param ids 主键串
     */
    @SaCheckPermission("itam:accessories:delete")
    @Log(title = "配件", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(accessoriesService.removeAccessoriesByIds(ids));
    }

    /**
     * 分配配件
     */
    @SaCheckPermission("itam:accessories:update")
    @Log(title = "配件", businessType = BusinessType.UPDATE)
    @PostMapping("/allocate")
    public Result<Boolean> allocate(@RequestBody AllocationsEntity allocations) {
        allocations.setItemType(AssetType.ACCESSORY);
        return Result.success(allocationsService.allocate(allocations));
    }

    /**
     * 取消分配配件
     */
    @SaCheckPermission("itam:accessories:update")
    @Log(title = "配件", businessType = BusinessType.UPDATE)
    @PostMapping("/deallocate/{id}")
    public Result<Boolean> deallocate(@PathVariable("id") Long id) {
        return Result.success(allocationsService.deallocate(id));
    }

    /**
     * 查询配件分配列表
     */
    @SaCheckPermission("itam:accessories:page")
    @GetMapping("/allocation/page")
    public Result<PageResultVO<AllocationsVO>> queryAllocationPage(PageRequest page, AllocationsPageReq req) {
        if (req == null) {
            req = new AllocationsPageReq();
        }
        req.setItemType(AssetType.ACCESSORY);
        return Result.success(new PageResultVO<>(allocationsService.queryPageVo(page.toMpPage(), req)));
    }

    /**
     * 获取当年内每月配件价值的趋势
     */
    @GetMapping("/stats/monthly-value")
    @SaCheckPermission("itam:accessories:page")
    public Result<List<AssetsReportVO>> getMonthlyValueStats() {
        return Result.success(assetsReportService.getMonthlyTrend(AssetType.ACCESSORY));
    }

    /**
     * 获取配件汇总统计数据
     */
    @GetMapping("/stats/summary")
    @SaCheckPermission("itam:accessories:page")
    public Result<Map<String, Object>> getSummaryStats() {
        return Result.success(accessoriesService.getSummaryStats());
    }

    /**
     * 报告故障
     */
    @SaCheckPermission("itam:accessories:update")
    @Log(title = "故障报告", businessType = BusinessType.UPDATE)
    @PostMapping("/report")
    public Result<Boolean> report(@RequestBody FailuresReportReq req) {
        failuresService.reportFailure(AssetType.ACCESSORY, req);
        return Result.success(true);
    }

}
