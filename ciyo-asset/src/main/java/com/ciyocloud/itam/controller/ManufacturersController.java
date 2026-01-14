package com.ciyocloud.itam.controller;

import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.ManufacturersEntity;
import com.ciyocloud.itam.service.ManufacturersService;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 制造商
 *
 * @author codeck
 * @since 2025-12-29 15:58:20
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/manufacturers")
public class ManufacturersController {

    private final ManufacturersService manufacturersService;

    /**
     * 查询制造商列表
     */
    @PreAuthorize("@ss.hasPermi('itam:manufacturers:page')")
    @GetMapping("/page")
    public Result<PageResultVO<ManufacturersEntity>> queryPage(PageRequest page, ManufacturersEntity manufacturers) {
        return Result.success(new PageResultVO<>(manufacturersService.page(page.toMpPage(), QueryWrapperUtils.toSimpleQuery(manufacturers))));
    }

    /**
     * 查询制造商列表
     */
    @PreAuthorize("@ss.hasPermi('itam:manufacturers:list')")
    @GetMapping("/list")
    public Result<List<ManufacturersEntity>> list(ManufacturersEntity manufacturers) {
        return Result.success(manufacturersService.list(QueryWrapperUtils.toSimpleQuery(manufacturers)));
    }

    /**
     * 导出制造商列表
     */
    @PreAuthorize("@ss.hasPermi('itam:manufacturers:export')")
    @Log(title = "制造商", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(ManufacturersEntity manufacturers) {
        List<ManufacturersEntity> list = manufacturersService.list(QueryWrapperUtils.toSimpleQuery(manufacturers));
        ExcelUtils.exportExcel(list, "制造商数据", ManufacturersEntity.class);
    }

    /**
     * 获取制造商详细信息
     *
     * @param id 主键
     */
    @PreAuthorize("@ss.hasPermi('itam:manufacturers:query')")
    @GetMapping(value = "/{id:\\d+}")
    public Result<ManufacturersEntity> getInfo(@PathVariable("id") Long id) {
        return Result.success(manufacturersService.getById(id));
    }

    /**
     * 新增制造商
     */
    @PreAuthorize("@ss.hasPermi('itam:manufacturers:add')")
    @Log(title = "制造商", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result<Boolean> add(@Validated(AddGroup.class) @RequestBody ManufacturersEntity manufacturers) {
        ValidatorUtils.validateEntity(manufacturers, AddGroup.class);
        return Result.success(manufacturersService.save(manufacturers));
    }

    /**
     * 修改制造商
     */
    @PreAuthorize("@ss.hasPermi('itam:manufacturers:update')")
    @Log(title = "制造商", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody ManufacturersEntity manufacturers) {
        ValidatorUtils.validateEntity(manufacturers, UpdateGroup.class);
        return Result.success(manufacturersService.updateById(manufacturers));
    }

    /**
     * 删除制造商
     *
     * @param ids 主键串
     */
    @PreAuthorize("@ss.hasPermi('itam:manufacturers:delete')")
    @Log(title = "制造商", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(manufacturersService.removeByIds(ids));
    }
}
