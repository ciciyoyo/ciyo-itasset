package com.ciyocloud.itam.controller;

import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.CategoriesEntity;
import com.ciyocloud.itam.service.CategoriesService;
import com.ciyocloud.itam.util.AssetPermissionUtils;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import com.ciyocloud.system.constant.UserConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 分类
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/categories")
public class CategoriesController {

    private final CategoriesService categoriesService;
    private final AssetPermissionUtils assetPermissionUtils;

    /**
     * 查询分类树结构
     */
    @GetMapping("/tree")
    public Result<List<CategoriesEntity>> tree(CategoriesEntity categories) {
        assetPermissionUtils.hasAssetPermi(categories.getCategoryType().getCode(), "page");
        return Result.success(categoriesService.listTree(categories));
    }

    /**
     * 导出分类列表
     */
    @Log(title = "分类", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(CategoriesEntity categories) {
        assetPermissionUtils.hasAssetPermi(categories.getCategoryType().getCode(), "export");
        List<CategoriesEntity> list = categoriesService.list(QueryWrapperUtils.toSimpleQuery(categories));
        ExcelUtils.exportExcel(list, "分类数据", CategoriesEntity.class);
    }

    /**
     * 获取分类详细信息
     *
     * @param id 主键
     */
    @GetMapping(value = "/{id}")
    public Result<CategoriesEntity> getInfo(@PathVariable Long id) {
        return Result.success(categoriesService.getById(id));
    }

    /**
     * 新增分类
     */
    @Log(title = "分类", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result<Boolean> add(@Validated(AddGroup.class) @RequestBody CategoriesEntity categories) {
        ValidatorUtils.validateEntity(categories, AddGroup.class);
        assetPermissionUtils.hasAssetPermi(categories.getCategoryType().getCode(), "add");
        if (UserConstants.NOT_UNIQUE.equals(categoriesService.checkCategoryCodeUnique(categories))) {
            return Result.failed("新增分类'" + categories.getName() + "'失败，分类编码已存在");
        }
        return Result.success(categoriesService.save(categories));
    }

    /**
     * 修改分类
     */
    @Log(title = "分类", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody CategoriesEntity categories) {
        ValidatorUtils.validateEntity(categories, UpdateGroup.class);
        assetPermissionUtils.hasAssetPermi(categories.getCategoryType().getCode(), "update");
        if (UserConstants.NOT_UNIQUE.equals(categoriesService.checkCategoryCodeUnique(categories))) {
            return Result.failed("修改分类'" + categories.getName() + "'失败，分类编码已存在");
        }
        return Result.success(categoriesService.updateById(categories));
    }

    /**
     * 删除分类
     *
     * @param ids 主键串
     */
    @Log(title = "分类", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids, @RequestParam String categorieType) {
        assetPermissionUtils.hasAssetPermi(categorieType, "delete");
        return Result.success(categoriesService.removeByIds(ids));
    }
}
