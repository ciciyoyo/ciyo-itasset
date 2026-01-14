package com.ciyocloud.api.web.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.codec.Base64Encoder;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.envconfig.service.SysEnvConfigService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ciyocloud.common.constant.ConfigConstants.SYSTEM_INFO_CONFIG;

/**
 * 公开接口
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/")
public class PublicController {
    private final SysEnvConfigService configService;


    /***
     * 获取系统信息配置
     * @return 系统信息配置
     */
    @GetMapping("systemInfoConfig")
    @SaIgnore
    public Result<String> getSystemInfoConfig() {
        // 系统配置
        String value = configService.getValueByKey(SYSTEM_INFO_CONFIG);
        return Result.success(Base64Encoder.encode(value));
    }


}

