package com.ciyocloud.itam.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.thread.ThreadUtil;
import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.ModelsEntity;
import com.ciyocloud.itam.service.ModelsService;
import com.ciyocloud.itam.vo.ModelsVO;
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
 * 型号
 *
 * @author codeck
 * @since 2026-01-01
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/models")
public class ModelsController {

    private final ModelsService modelsService;

    /**
     * 查询型号列表
     */
    @SaCheckPermission("itam:models:page")
    @GetMapping("/page")
    public Result<PageResultVO<ModelsVO>> queryPage(PageRequest page, ModelsEntity models) {
        return Result.success(new PageResultVO<>(modelsService.selectPageVo(page.toMpPage(), models)));
    }

    /**
     * 查询型号列表
     */
    @SaCheckPermission("itam:models:page")
    @GetMapping("/list")
    public Result<List<ModelsVO>> list(ModelsEntity models) {
        return Result.success(modelsService.selectListVo(models));
    }

    /**
     * 导出型号列表
     */
    @SaCheckPermission("itam:models:export")
    @Log(title = "型号", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(ModelsEntity models) {
        List<ModelsVO> list = modelsService.selectListVo(models);
        ExcelUtils.exportExcel(list, "型号数据", ModelsVO.class);
    }

    /**
     * 获取型号详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("itam:models:query")
    @GetMapping(value = "/{id:\\d+}")
    public Result<ModelsEntity> getInfo(@PathVariable("id") Long id) {
        return Result.success(modelsService.getById(id));
    }

    /**
     * 新增型号
     */
    @SaCheckPermission("itam:models:add")
    @Log(title = "型号", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result<Boolean> add(@Validated(AddGroup.class) @RequestBody ModelsEntity models) {
        ValidatorUtils.validateEntity(models, AddGroup.class);
        models.setCategoryId(0L);
        return Result.success(modelsService.save(models));
    }

    /**
     * 修改型号
     */
    @SaCheckPermission("itam:models:update")
    @Log(title = "型号", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody ModelsEntity models) {
        ValidatorUtils.validateEntity(models, UpdateGroup.class);
        return Result.success(modelsService.updateById(models));
    }

    /**
     * 删除型号
     *
     * @param ids 主键串
     */
    @SaCheckPermission("itam:models:delete")
    @Log(title = "型号", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(modelsService.removeByIds(ids));
    }

    /**
     * SSE进度导入型号列表
     *
     * @param file        导入文件
     * @param progressKey 前端传递的进度监听key
     */
    @SaCheckPermission("itam:models:import")
    @Log(title = "型号", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public Result<String> importData(MultipartFile file, @RequestParam String progressKey) throws Exception {
        Long userId = SecurityUtils.getUserId();
        ThreadUtil.execute(() -> {
            // 异步导入会删除文件 这里要转换到新的流
            try (var inputStream = new ByteArrayInputStream(file.getBytes())) {
                modelsService.importData(inputStream, file.getOriginalFilename(), progressKey, userId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return Result.success();
    }
}
