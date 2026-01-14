package com.ciyocloud.api.web.controller.system;

import com.ciyocloud.common.entity.SystemEnvConfig;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.envconfig.service.SysEnvConfigService;
import com.ciyocloud.system.constant.SysConfigConstants;
import com.ciyocloud.system.constant.SystemConstants;
import com.ciyocloud.system.request.RegisterRequest;
import com.ciyocloud.system.service.SysConfigService;
import com.ciyocloud.system.service.security.SysRegisterService;
import com.ciyocloud.system.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * 注册验证
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
public class SysRegisterController {
    private final SysRegisterService registerService;
    private final SysEnvConfigService envConfigService;
    private final SysConfigService configService;

    /**
     * 注册方法
     *
     * @param request 注册信息
     * @return 结果
     */
    @PostMapping("/register")
    public Result register(@Validated @RequestBody RegisterRequest request) {
        PasswordUtils.checkPassword(request.getPassword());
        ValidatorUtils.validateEntity(request);
        Optional.of(envConfigService.getSystemEnvConfig()).map(SystemEnvConfig::isEnableSmsVerification).ifPresent(enableSmsVerification -> {
            if (enableSmsVerification) {
              /*  // 检查手机号验证码是否正确
                if (!formPhoneSmsService.checkPhoneCode(request.getPhonenumber(), request.getCode())) {
                    throw new BusinessException("手机号验证码错误，请重新输入！");
                }*/
            }
        });
        if (!SystemConstants.YES_STATUS.equalsIgnoreCase(configService.getConfigValueByKey(SysConfigConstants.REGISTER_ENABLE))) {
            return Result.failed("暂未开启注册功能，请联系管理员！");
        }
        return registerService.register(request);
    }
}
