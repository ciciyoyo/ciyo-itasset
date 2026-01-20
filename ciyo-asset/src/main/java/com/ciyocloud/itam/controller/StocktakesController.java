package com.ciyocloud.itam.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.StocktakesEntity;
import com.ciyocloud.itam.enums.StocktakeStatus;
import com.ciyocloud.itam.req.StocktakesPageReq;
import com.ciyocloud.itam.service.StocktakesService;
import com.ciyocloud.itam.vo.StocktakesDetailVO;
import com.ciyocloud.itam.vo.StocktakesVO;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 盘点任务
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/stocktakes")
public class StocktakesController {

    private final StocktakesService stocktakesService;

    /**
     * 查询盘点任务列表
     */
    @SaCheckPermission("itam:stocktakes:page")
    @GetMapping("/page")
    public Result<PageResultVO<StocktakesVO>> queryPage(PageRequest page, StocktakesPageReq req) {
        return Result.success(new PageResultVO<>(stocktakesService.queryPageVo(page.toMpPage(), req)));
    }

    /**
     * 导出盘点任务列表
     */
    @SaCheckPermission("itam:stocktakes:export")
    @Log(title = "盘点任务", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(StocktakesPageReq req) {
        List<StocktakesVO> list = stocktakesService.queryListVo(req);
        ExcelUtils.exportExcel(list, "盘点任务数据", StocktakesVO.class);
    }

    /**
     * 获取盘点任务详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("itam:stocktakes:query")
    @GetMapping(value = "/{id}")
    public Result<StocktakesDetailVO> getInfo(@PathVariable("id") Long id) {
        return Result.success(stocktakesService.getDetailVo(id));
    }

    /**
     * 新增盘点任务
     */
    @SaCheckPermission("itam:stocktakes:add")
    @Log(title = "盘点任务", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result<Boolean> add(@Validated(AddGroup.class) @RequestBody StocktakesEntity stocktakes) {
        ValidatorUtils.validateEntity(stocktakes, AddGroup.class);
        stocktakes.setStatus(StocktakeStatus.PROCESSING);
        stocktakes.setManagerId(SecurityUtils.getUserId());
        return Result.success(stocktakesService.add(stocktakes));
    }

    /**
     * 修改盘点任务
     */
    @SaCheckPermission("itam:stocktakes:update")
    @Log(title = "盘点任务", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody StocktakesEntity stocktakes) {
        ValidatorUtils.validateEntity(stocktakes, UpdateGroup.class);
        return Result.success(stocktakesService.updateById(stocktakes));
    }

    /**
     * 删除盘点任务
     *
     * @param ids 主键串
     */
    @SaCheckPermission("itam:stocktakes:delete")
    @Log(title = "盘点任务", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(stocktakesService.removeByIds(ids));
    }
}
