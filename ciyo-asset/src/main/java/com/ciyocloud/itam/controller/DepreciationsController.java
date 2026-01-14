package com.ciyocloud.itam.controller;

import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.DepreciationsEntity;
import com.ciyocloud.itam.service.DepreciationsService;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 折旧规则
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/depreciations")
public class DepreciationsController {

    private final DepreciationsService depreciationsService;

    /**
     * 查询折旧规则列表
     */
    @PreAuthorize("@ss.hasPermi('itam:depreciations:page')")
    @GetMapping("/page")
    public Result<PageResultVO<DepreciationsEntity>> queryPage(PageRequest page, DepreciationsEntity depreciations) {
        return Result.success(new PageResultVO<>(depreciationsService.page(page.toMpPage(), QueryWrapperUtils.toSimpleQuery(depreciations))));
    }

    /**
     * 查询折旧规则列表
     */
    @PreAuthorize("@ss.hasPermi('itam:depreciations:page')")
    @GetMapping("/list")
    public Result<List<DepreciationsEntity>> list(DepreciationsEntity depreciations) {
        return Result.success(depreciationsService.list(QueryWrapperUtils.toSimpleQuery(depreciations)));
    }

    /**
     * 导出折旧规则列表
     */
    @PreAuthorize("@ss.hasPermi('itam:depreciations:export')")
    @Log(title = "折旧规则", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(DepreciationsEntity depreciations) {
        List<DepreciationsEntity> list = depreciationsService.list(QueryWrapperUtils.toSimpleQuery(depreciations));
        ExcelUtils.exportExcel(list, "折旧规则数据", DepreciationsEntity.class);
    }

    /**
     * 获取折旧规则详细信息
     *
     * @param id 主键
     */
    @PreAuthorize("@ss.hasPermi('itam:depreciations:query')")
    @GetMapping(value = "/{id:\\d+}")
    public Result<DepreciationsEntity> getInfo(@PathVariable("id") Long id) {
        return Result.success(depreciationsService.getById(id));
    }

    /**
     * 新增折旧规则
     */
    @PreAuthorize("@ss.hasPermi('itam:depreciations:add')")
    @Log(title = "折旧规则", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result<Boolean> add(@Validated(AddGroup.class) @RequestBody DepreciationsEntity depreciations) {
        ValidatorUtils.validateEntity(depreciations, AddGroup.class);
        if (depreciations.getStatus() == null) {
            depreciations.setStatus(1L);
        }
        if (depreciations.getFloorType() == null) {
            depreciations.setFloorType("percent");
        }
        if (depreciations.getFloorVal() == null) {
            depreciations.setFloorVal(java.math.BigDecimal.ZERO);
        }
        if (depreciations.getStages() != null) {
            long totalMonths = 0;
            for (DepreciationsEntity.RulePeriod stage : depreciations.getStages()) {
                if (stage.getPeriod() != null && stage.getUnit() != null) {
                    switch (stage.getUnit()) {
                        case YEAR:
                            totalMonths += stage.getPeriod() * 12;
                            break;
                        case MONTH:
                            totalMonths += stage.getPeriod();
                            break;
                        case DAY:
                            totalMonths += stage.getPeriod() / 30;
                            break;
                    }
                }
            }
            depreciations.setMonths(totalMonths);
        }
        return Result.success(depreciationsService.save(depreciations));
    }

    /**
     * 修改折旧规则
     */
    @PreAuthorize("@ss.hasPermi('itam:depreciations:update')")
    @Log(title = "折旧规则", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody DepreciationsEntity depreciations) {
        ValidatorUtils.validateEntity(depreciations, UpdateGroup.class);
        return Result.success(depreciationsService.updateById(depreciations));
    }

    /**
     * 删除折旧规则
     *
     * @param ids 主键串
     */
    @PreAuthorize("@ss.hasPermi('itam:depreciations:delete')")
    @Log(title = "折旧规则", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(depreciationsService.removeByIds(ids));
    }
}
