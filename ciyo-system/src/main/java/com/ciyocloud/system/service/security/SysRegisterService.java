package com.ciyocloud.system.service.security;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.request.RegisterRequest;
import com.ciyocloud.system.service.SysConfigService;
import com.ciyocloud.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * 注册
 *
 * @author codeck
 */
@Component
@RequiredArgsConstructor
public class SysRegisterService {
    private final SysUserService userService;

    private final SysConfigService configService;


    /**
     * 注册
     */
    public Result<Long> register(RegisterRequest request) {
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(request.getUserName()))) {
            return Result.failed("保存账号'" + request.getUserName() + "'失败，注册账号已存在");
        }
        SysUserEntity phoneUser = new SysUserEntity();
        phoneUser.setPhonenumber(request.getPhonenumber());
        if (StrUtil.isNotBlank(request.getPhonenumber()) && !UserConstants.UNIQUE.equalsIgnoreCase(userService.checkPhoneUnique(phoneUser))) {
            return Result.failed("手机号已存在，请更换手机号后再试！");
        }
        SysUserEntity sysUser = new SysUserEntity();
        sysUser.setUserName(request.getUserName());
        sysUser.setPassword(request.getPassword());
        sysUser.setNickName(StrUtil.isBlank(request.getNickName()) ? request.getUserName() : request.getNickName());
        sysUser.setEmail(request.getEmail());
        sysUser.setPhonenumber(request.getPhonenumber());
        sysUser.setSex(request.getSex());
        //默认注册用户角色
        Long userId = userService.insertUserDefaultData(sysUser);
        if (ObjectUtil.isNull(userId)) {
            return Result.failed("注册失败,请联系系统管理员！");
        }
        return Result.success(userId);
    }

}
