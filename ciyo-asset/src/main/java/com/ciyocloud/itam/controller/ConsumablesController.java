package com.ciyocloud.itam.controller;

import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.ConsumableTransactionsEntity;
import com.ciyocloud.itam.entity.ConsumablesEntity;
import com.ciyocloud.itam.req.ConsumableStockReq;
import com.ciyocloud.itam.service.ConsumableTransactionsService;
import com.ciyocloud.itam.service.ConsumablesService;
import com.ciyocloud.itam.vo.ConsumableTransactionsVO;
import com.ciyocloud.itam.vo.ConsumablesVO;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 耗材
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/consumables")
public class ConsumablesController {

    private final ConsumablesService consumablesService;
    private final ConsumableTransactionsService consumableTransactionsService;

    /**
     * 查询耗材列表
     */
    @SaCheckPermission("itam:consumables:page")
    @GetMapping("/page")
    public Result<PageResultVO<ConsumablesVO>> queryPage(PageRequest page, ConsumablesEntity consumables) {
        return Result.success(new PageResultVO<>(consumablesService.queryPageVo(page.toMpPage(), consumables)));
    }

    /**
     * 查询耗材列表
     */
    @SaCheckPermission("itam:consumables:page")
    @GetMapping("/list")
    public Result<List<ConsumablesEntity>> list(ConsumablesEntity consumables) {
        return Result.success(consumablesService.list(QueryWrapperUtils.toSimpleQuery(consumables)));
    }

    /**
     * 导出耗材列表
     */
    @SaCheckPermission("itam:consumables:export")
    @Log(title = "耗材", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(ConsumablesEntity consumables) {
        List<ConsumablesVO> list = consumablesService.queryListVo(consumables);
        ExcelUtils.exportExcel(list, "耗材数据", ConsumablesVO.class);
    }

    /**
     * 获取耗材详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("itam:consumables:query")
    @GetMapping(value = "/{id:\\d+}")
    public Result<ConsumablesEntity> getInfo(@PathVariable("id") Long id) {
        return Result.success(consumablesService.getById(id));
    }

    /**
     * 新增耗材
     */
    @SaCheckPermission("itam:consumables:add")
    @Log(title = "耗材", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result<Boolean> add(@Validated(AddGroup.class) @RequestBody ConsumablesEntity consumables) {
        ValidatorUtils.validateEntity(consumables, AddGroup.class);
        return Result.success(consumablesService.save(consumables));
    }

    /**
     * 修改耗材
     */
    @SaCheckPermission("itam:consumables:update")
    @Log(title = "耗材", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody ConsumablesEntity consumables) {
        ValidatorUtils.validateEntity(consumables, UpdateGroup.class);
        return Result.success(consumablesService.updateById(consumables));
    }

    /**
     * 删除耗材
     *
     * @param ids 主键串
     */
    @SaCheckPermission("itam:consumables:delete")
    @Log(title = "耗材", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(consumablesService.removeConsumablesByIds(ids));
    }

    /**
     * 耗材入库
     */
    @SaCheckPermission("itam:consumables:update")
    @Log(title = "耗材", businessType = BusinessType.UPDATE)
    @PostMapping("/stockIn")
    public Result<Boolean> stockIn(@Validated @RequestBody ConsumableStockReq req) {
        ConsumableTransactionsEntity transaction = new ConsumableTransactionsEntity();
        BeanUtils.copyProperties(req, transaction);
        transaction.setOperatorId(SecurityUtils.getUserId());
        return Result.success(consumablesService.stockIn(transaction));
    }

    /**
     * 耗材领取
     */
    @SaCheckPermission("itam:consumables:update")
    @Log(title = "耗材", businessType = BusinessType.UPDATE)
    @PostMapping("/collect")
    public Result<Boolean> collect(@Validated @RequestBody ConsumableStockReq req) {
        ConsumableTransactionsEntity transaction = new ConsumableTransactionsEntity();
        BeanUtils.copyProperties(req, transaction);
        transaction.setOperatorId(SecurityUtils.getUserId());
        return Result.success(consumablesService.stockOut(transaction));
    }


    /**
     * 查询耗材出入库明细列表
     */
    @SaCheckPermission("itam:consumables:query")
    @GetMapping("/transactions/page")
    public Result<PageResultVO<ConsumableTransactionsVO>> queryTransactionsPage(PageRequest page, ConsumableTransactionsEntity transaction) {
        return Result.success(new PageResultVO<>(consumableTransactionsService.queryPageVo(page.toMpPage(), transaction)));
    }

    /**
     * 获取耗材分类统计
     */
    @GetMapping("/stats/category")
    @SaCheckPermission("itam:consumables:page")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        return Result.success(consumablesService.getCategoryStats());
    }

    /**
     * 获取耗材总览统计
     */
    @GetMapping("/stats/overview")
    @SaCheckPermission("itam:consumables:page")
    public Result<Map<String, Object>> getOverviewStats() {
        return Result.success(consumablesService.getOverviewStats());
    }

    /**
     * 获取耗材月度出入库统计
     */
    @GetMapping("/stats/monthly")
    @SaCheckPermission("itam:consumables:page")
    public Result<List<Map<String, Object>>> getMonthlyStats() {
        return Result.success(consumableTransactionsService.getMonthlyStats());
    }

}
