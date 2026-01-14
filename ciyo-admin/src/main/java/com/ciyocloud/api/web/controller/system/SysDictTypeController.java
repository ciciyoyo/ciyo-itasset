package com.ciyocloud.api.web.controller.system;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysDictTypeEntity;
import com.ciyocloud.system.service.SysDictTypeService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典信息
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dict/type")
public class SysDictTypeController {
    private final SysDictTypeService dictTypeService;


    /**
     * 分页字典类型列表
     */
    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @GetMapping("/page")
    public Result queryPage(Page page, SysDictTypeEntity dictType) {
        return Result.success(dictTypeService.page(page, QueryWrapperUtils.toSimpleQuery(dictType)));
    }


    /**
     * 导出字典类型列表
     *
     * @param dictType 字典类型
     */
    @Log(title = "字典类型", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:dict:export')")
    @GetMapping("/export")
    public void export(SysDictTypeEntity dictType) {
        List<SysDictTypeEntity> list = dictTypeService.list(QueryWrapperUtils.toSimpleQuery(dictType));
        ExcelUtils.exportExcel(list, "字典类型", SysDictTypeEntity.class);
    }


    /**
     * 获取字典选择框列表
     */
    @GetMapping("/optionselect")
    public Result optionselect() {
        List<SysDictTypeEntity> dictTypes = dictTypeService.list();
        return Result.success(dictTypes);
    }

    /**
     * 查询字典类型详细
     */
    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @GetMapping(value = "/{dictId:\\d+}")
    @PermitAll
    public Result getInfo(@PathVariable Long dictId) {
        return Result.success(dictTypeService.getById(dictId));
    }

    /**
     * 新增字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    @PostMapping
    public Result save(@Validated @RequestBody SysDictTypeEntity dict) {
        if (UserConstants.NOT_UNIQUE.equals(dictTypeService.checkDictTypeUnique(dict))) {
            return Result.failed("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        dict.setCreateBy(SecurityUtils.getUserId());
        return Result.success(dictTypeService.saveDictType(dict));
    }

    /**
     * 修改字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:edit')")
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result update(@Validated @RequestBody SysDictTypeEntity dict) {
        if (UserConstants.NOT_UNIQUE.equals(dictTypeService.checkDictTypeUnique(dict))) {
            return Result.failed("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        dict.setUpdateBy(SecurityUtils.getUserId());
        return Result.success(dictTypeService.updateDictType(dict));
    }

    /**
     * 删除字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dictIds}")
    public Result delete(@PathVariable List<Long> dictIds) {
        dictTypeService.removeByIds(dictIds);
        return Result.success();
    }

    /**
     * 刷新字典缓存
     */
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public Result refreshCache() {
        dictTypeService.resetDictCache();
        return Result.success();
    }


}
