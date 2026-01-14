package com.ciyocloud.api.web.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import com.ciyocloud.system.entity.SysDictDataEntity;
import com.ciyocloud.system.service.SysDictDataService;
import com.ciyocloud.system.service.SysDictTypeService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据字典信息
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dict/data")
public class SysDictDataController {
    private final SysDictDataService dictDataService;

    private final SysDictTypeService dictTypeService;


    /**
     * 分页字典数据列表
     */
    @SaCheckPermission("system:dict:list")
    @GetMapping("/page")
    public Result<Page<SysDictDataEntity>> queryPage(Page<SysDictDataEntity> page, SysDictDataEntity dictData) {
        return Result.success(dictDataService.page(page, Wrappers.lambdaQuery(SysDictDataEntity.class)
                .like(StrUtil.isNotBlank(dictData.getDictLabel()), SysDictDataEntity::getDictLabel, dictData.getDictLabel())
                .eq(null != dictData.getStatus(), SysDictDataEntity::getStatus, dictData.getStatus())
                .eq(StrUtil.isNotBlank(dictData.getDictType()), SysDictDataEntity::getDictType, dictData.getDictType())));
    }

    /**
     * 导出字典数据列表
     *
     * @param dictData 字典数据
     */
    @Log(title = "字典数据", businessType = BusinessType.EXPORT)
    @SaCheckPermission("system:dict:export")
    @GetMapping("/export")
    public void export(SysDictDataEntity dictData) {
        List<SysDictDataEntity> list = dictDataService.list(Wrappers.lambdaQuery(SysDictDataEntity.class)
                .like(StrUtil.isNotBlank(dictData.getDictLabel()), SysDictDataEntity::getDictLabel, dictData.getDictLabel())
                .eq(null != dictData.getStatus(), SysDictDataEntity::getStatus, dictData.getStatus())
                .eq(StrUtil.isNotBlank(dictData.getDictType()), SysDictDataEntity::getDictType, dictData.getDictType()));
        ExcelUtils.exportExcel(list, "字典数据", SysDictDataEntity.class);
    }

    /**
     * 查询字典数据详细
     */
    @SaCheckPermission("system:dict:query")
    @GetMapping(value = "/{dictCode}")
    public Result getInfo(@PathVariable Long dictCode) {
        return Result.success(dictDataService.getById(dictCode));
    }

    /**
     * 根据字典类型查询字典数据信息
     */
    @GetMapping(value = "/type/{dictType}")
    @SaIgnore
    public Result dictType(@PathVariable String dictType) {
        List<SysDictDataEntity> data = dictTypeService.getDictDataByType(dictType);
        if (ObjectUtil.isNull(data)) {
            data = new ArrayList<>();
        }
        return Result.success(data);
    }

    /**
     * 新增字典类型
     */
    @SaCheckPermission("system:dict:add")
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    @PostMapping
    public Result save(@Validated @RequestBody SysDictDataEntity dict) {
        dict.setCreateBy(SecurityUtils.getUserId());
        return Result.success(dictDataService.saveDictData(dict));
    }

    /**
     * 修改保存字典类型
     */
    @SaCheckPermission("system:dict:edit")
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result update(@Validated @RequestBody SysDictDataEntity dict) {
        dict.setUpdateBy(SecurityUtils.getUserId());
        return Result.success(dictDataService.updateDictData(dict));
    }

    /**
     * 删除字典类型
     */
    @SaCheckPermission("system:dict:remove")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dictCodes}")
    public Result delete(@PathVariable List<Long> dictCodes) {
        dictDataService.deleteDictDataByIds(dictCodes);
        return Result.success();
    }
}
