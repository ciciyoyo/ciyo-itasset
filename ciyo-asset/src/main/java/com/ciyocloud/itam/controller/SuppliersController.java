package com.ciyocloud.itam.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.thread.ThreadUtil;
import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.SuppliersEntity;
import com.ciyocloud.itam.service.SuppliersService;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;


/**
 * 供应商
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/suppliers")
public class SuppliersController {

    private final SuppliersService suppliersService;

    /**
     * 查询供应商列表
     */
    @SaCheckPermission("itam:suppliers:page")
    @GetMapping("/page")
    public Result<PageResultVO<SuppliersEntity>> queryPage(PageRequest page, SuppliersEntity suppliers) {
        return Result.success(new PageResultVO<>(suppliersService.page(page.toMpPage(), QueryWrapperUtils.toSimpleQuery(suppliers))));
    }

    /**
     * 查询供应商列表
     */
    @SaCheckPermission("itam:suppliers:page")
    @GetMapping("/list")
    public Result<List<SuppliersEntity>> list(SuppliersEntity suppliers) {
        return Result.success(suppliersService.list(QueryWrapperUtils.toSimpleQuery(suppliers)));
    }

    /**
     * 导出供应商列表
     */
    @SaCheckPermission("itam:suppliers:export")
    @Log(title = "供应商", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(SuppliersEntity suppliers) {
        List<SuppliersEntity> list = suppliersService.list(QueryWrapperUtils.toSimpleQuery(suppliers));
        ExcelUtils.exportExcel(list, "供应商数据", SuppliersEntity.class);
    }

    /**
     * 获取供应商详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("itam:suppliers:query")
    @GetMapping(value = "/{id:\\d+}")
    public Result<SuppliersEntity> getInfo(@PathVariable("id") Long id) {
        return Result.success(suppliersService.getById(id));
    }

    /**
     * 新增供应商
     */
    @SaCheckPermission("itam:suppliers:add")
    @Log(title = "供应商", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result<Boolean> add(@Validated(AddGroup.class) @RequestBody SuppliersEntity suppliers) {
        ValidatorUtils.validateEntity(suppliers, AddGroup.class);
        return Result.success(suppliersService.save(suppliers));
    }

    /**
     * 修改供应商
     */
    @SaCheckPermission("itam:suppliers:update")
    @Log(title = "供应商", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody SuppliersEntity suppliers) {
        ValidatorUtils.validateEntity(suppliers, UpdateGroup.class);
        return Result.success(suppliersService.updateById(suppliers));
    }

    /**
     * 删除供应商
     *
     * @param ids 主键串
     */
    @SaCheckPermission("itam:suppliers:delete")
    @Log(title = "供应商", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(suppliersService.removeByIds(ids));
    }

    /**
     * SSE进度导入供应商列表
     *
     * @param file        导入文件
     * @param progressKey 前端传递的进度监听key
     */
    @SaCheckPermission("itam:suppliers:import")
    @Log(title = "供应商", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public Result<String> importData(MultipartFile file, @RequestParam String progressKey) throws Exception {
        Long userId = SecurityUtils.getUserId();
        ThreadUtil.execute(() -> {
            // 异步导入会删除文件 这里要转换到新的流
            try (var inputStream = new ByteArrayInputStream(file.getBytes())) {
                suppliersService.importData(inputStream, file.getOriginalFilename(), progressKey, userId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return Result.success();
    }
}
