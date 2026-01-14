package com.ciyocloud.itam.controller;

import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.StocktakeItemsEntity;
import com.ciyocloud.itam.req.StocktakeItemsPageReq;
import com.ciyocloud.itam.service.StocktakeItemsService;
import com.ciyocloud.itam.vo.StocktakeItemsVO;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 盘点明细
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/stocktakeItems")
public class StocktakeItemsController {

    private final StocktakeItemsService stocktakeItemsService;

    /**
     * 查询盘点明细列表
     */
    @PreAuthorize("@ss.hasPermi('itam:stocktakeItems:page')")
    @GetMapping("/page")
    public Result<PageResultVO<StocktakeItemsVO>> queryPage(PageRequest page, StocktakeItemsPageReq req) {
        return Result.success(new PageResultVO<>(stocktakeItemsService.queryPageVo(page.toMpPage(), req)));
    }

    /**
     * 导出盘点明细列表
     */
    @PreAuthorize("@ss.hasPermi('itam:stocktakeItems:export')")
    @Log(title = "盘点明细", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(StocktakeItemsPageReq req) {
        List<StocktakeItemsVO> list = stocktakeItemsService.queryListVo(req);
        ExcelUtils.exportExcel(list, "盘点明细数据", StocktakeItemsVO.class);
    }


    /**
     * 删除盘点明细
     *
     * @param ids 主键串
     */
    @PreAuthorize("@ss.hasPermi('itam:stocktakeItems:delete')")
    @Log(title = "盘点明细", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(stocktakeItemsService.removeByIds(ids));
    }

    /**
     * 更新盘点明细状态
     *
     * @param stocktakeItems 盘点明细
     */
    @PreAuthorize("@ss.hasPermi('itam:stocktakeItems:edit')")
    @Log(title = "盘点明细", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public Result<Boolean> edit(@RequestBody StocktakeItemsEntity stocktakeItems) {
        ValidatorUtils.validateEntity(stocktakeItems, UpdateGroup.class);
        return Result.success(stocktakeItemsService.updateStocktakeItem(stocktakeItems));
    }
}
