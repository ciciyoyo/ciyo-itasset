package com.ciyocloud.itam.controller;

import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.LocationsEntity;
import com.ciyocloud.itam.service.LocationsService;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 物理位置
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/locations")
public class LocationsController {

    private final LocationsService locationsService;

    /**
     * 查询物理位置列表
     */
    @SaCheckPermission("itam:locations:page")
    @GetMapping("/page")
    public Result<PageResultVO<LocationsEntity>> queryPage(PageRequest page, LocationsEntity locations) {
        return Result.success(new PageResultVO<>(locationsService.page(page.toMpPage(), QueryWrapperUtils.toSimpleQuery(locations))));
    }


    /**
     * 获取物理位置树
     */
    @SaCheckPermission("itam:locations:query")
    @GetMapping("/tree")
    public Result<List<LocationsEntity>> getTree(LocationsEntity locations) {
        return Result.success(locationsService.getTree(locations));
    }

    /**
     * 导出物理位置列表
     */
    @SaCheckPermission("itam:locations:export")
    @Log(title = "物理位置", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(LocationsEntity locations) {
        List<LocationsEntity> list = locationsService.list(QueryWrapperUtils.toSimpleQuery(locations));
        ExcelUtils.exportExcel(list, "物理位置数据", LocationsEntity.class);
    }

    /**
     * 获取物理位置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("itam:locations:query")
    @GetMapping(value = "/{id}")
    public Result<LocationsEntity> getInfo(@PathVariable("id") Long id) {
        return Result.success(locationsService.getById(id));
    }

    /**
     * 新增物理位置
     */
    @SaCheckPermission("itam:locations:add")
    @Log(title = "物理位置", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result<Boolean> add(@Validated(AddGroup.class) @RequestBody LocationsEntity locations) {
        ValidatorUtils.validateEntity(locations, AddGroup.class);
        return Result.success(locationsService.save(locations));
    }

    /**
     * 修改物理位置
     */
    @SaCheckPermission("itam:locations:update")
    @Log(title = "物理位置", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody LocationsEntity locations) {
        ValidatorUtils.validateEntity(locations, UpdateGroup.class);
        return Result.success(locationsService.updateById(locations));
    }

    /**
     * 删除物理位置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("itam:locations:delete")
    @Log(title = "物理位置", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(locationsService.removeByIds(ids));
    }
}
