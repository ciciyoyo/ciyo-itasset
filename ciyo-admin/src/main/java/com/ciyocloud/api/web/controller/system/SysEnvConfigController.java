package com.ciyocloud.api.web.controller.system;

import com.ciyocloud.common.util.Result;
import com.ciyocloud.envconfig.entity.SysEnvConfigEntity;
import com.ciyocloud.envconfig.service.SysEnvConfigService;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 参数配置 信息操作处理
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/env/config")
public class SysEnvConfigController {
    private final SysEnvConfigService configService;


    /**
     * 保存系统环境配置
     */
    @SaCheckPermission("system:envconfig:save")
    @Log(title = "修改系统配置", businessType = BusinessType.OTHER, isSaveRequestData = false)
    @PostMapping("/save")
    public Result save(@RequestBody @Validated SysEnvConfigEntity config) {
        configService.saveConfig(config);
        return Result.success();
    }

    /**
     * 根据参数编号获取详细信息
     */
    @GetMapping(value = "/{configKey}")
    @SaCheckPermission("system:envconfig:save")
    public Result getInfo(@PathVariable String configKey) {
        return Result.success(configService.getByKey(configKey));
    }


    /**
     * 根据参数编号获取详细信息
     */
    @GetMapping(value = "/getWxMiniAppId")
    public Result getWxMiniAppId() {
        SysEnvConfigEntity wxMaEnvConfig = configService.getByKey("wxMaEnvConfig");
        if (null == wxMaEnvConfig || null == wxMaEnvConfig.getEnvValue()) {
            return Result.success();
        }
        return Result.success(wxMaEnvConfig.getEnvValue().get("appid"));
    }

    /**
     * 根据参数编号获取详细信息
     */
    @GetMapping(value = "/value/{configKey}")
    @SaCheckPermission("system:envconfig:save")
    public Result getConfigValue(@PathVariable String configKey) {
        return Result.success(configService.getValueByKey(configKey));
    }

}
