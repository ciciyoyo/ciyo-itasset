package com.ciyocloud.api.web.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysConfigEntity;
import com.ciyocloud.system.service.SysConfigService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参数配置
 * <p>信息操作处理</p>
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/config")
public class SysConfigController {
    private final SysConfigService configService;


    /**
     * 获取参数配置列表
     */

    @SaCheckPermission("system:config:list")
    @GetMapping("/page")
    public Result queryPage(Page page, SysConfigEntity config) {
        return Result.success(configService.page(page, QueryWrapperUtils.toSimpleQuery(config)));
    }


    @SaCheckPermission("system:config:export")
    @Log(title = "参数管理", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(SysConfigEntity config) {
        List<SysConfigEntity> list = configService.list(QueryWrapperUtils.toSimpleQuery(config));
        ExcelUtils.exportExcel(list, "参数数据", SysConfigEntity.class);
    }

    /**
     * 根据参数编号获取详细信息
     */
    @SaCheckPermission("system:config:query")
    @GetMapping(value = "/{configId}")
    public Result getInfo(@PathVariable Long configId) {
        return Result.success(configService.getById(configId));
    }

    /**
     * 根据参数键key查询参数对象
     */
    @GetMapping(value = "/{configKey}/obj")
    public Result getConfig(@PathVariable String configKey) {
        return Result.success(configService.getConfigByKey(configKey));
    }

    /**
     * 根据参数键名查询参数值
     */
    @GetMapping(value = "/{configKey}/configValue")
    @SaIgnore
    public Result getConfigValue(@PathVariable String configKey) {
        return Result.success(configService.getConfigValueByKey(configKey));
    }

    /**
     * 新增参数配置
     */
    @SaCheckPermission("system:config:add")
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    @PostMapping
    public Result save(@Validated @RequestBody SysConfigEntity config) {
        if (UserConstants.NOT_UNIQUE.equals(configService.checkConfigKeyUnique(config))) {
            return Result.failed("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        config.setCreateBy(SecurityUtils.getUserId());
        if (null == config.getConfigType()) {
            config.setConfigType("N");
        }
        return Result.success(configService.saveConfig(config));
    }

    /**
     * 修改参数配置
     */
    @SaCheckPermission("system:config:edit")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result update(@Validated @RequestBody SysConfigEntity config) {
        if (UserConstants.NOT_UNIQUE.equals(configService.checkConfigKeyUnique(config))) {
            return Result.failed("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        config.setUpdateBy(SecurityUtils.getUserId());
        return Result.success(configService.updateConfig(config));
    }

    /**
     * 删除参数配置
     */
    @SaCheckPermission("system:config:remove")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public Result delete(@PathVariable List<Long> configIds) {
        configService.deleteConfigByIds(configIds);
        return Result.success();
    }

    /**
     * 刷新参数缓存
     */
    @SaCheckPermission("system:config:remove")
    @Log(title = "参数管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public Result refreshCache() {
        configService.resetConfigCache();
        return Result.success();
    }
}
