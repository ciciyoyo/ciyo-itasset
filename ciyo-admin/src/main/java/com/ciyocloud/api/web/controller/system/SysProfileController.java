package com.ciyocloud.api.web.controller.system;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.entity.security.LoginUserEntity;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.common.util.ServletUtils;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import com.ciyocloud.storage.cloud.OssStorageFactory;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.request.UpdatePwdRequest;
import com.ciyocloud.system.service.SysUserService;
import com.ciyocloud.system.service.security.TokenService;
import com.ciyocloud.system.util.LoginUtils;
import com.ciyocloud.system.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * 个人信息
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user/profile")
public class SysProfileController {
    private final SysUserService userService;

    private final TokenService tokenService;

    private final LoginUtils loginUtils;

    /**
     * 个人信息
     */
    @GetMapping
    public Result profile() {
        String username = SecurityUtils.getUsername();
        Map<String, Object> result = new HashMap<>(2);
        result.put("user", userService.getUserByUserId(SecurityUtils.getUserId()));
        result.put("postGroup", userService.getUserPostGroup(username));
        return Result.success(result);
    }

    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result updateProfile(@RequestBody SysUserEntity user) {
        if (StrUtil.isNotEmpty(user.getPhonenumber()) && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return Result.failed("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        if (StrUtil.isNotEmpty(user.getEmail()) && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return Result.failed("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        // 账号不允许修改
        user.setUserName(null);
        userService.checkUserDataScope(user.getId());
        if (userService.updateUserProfile(user) > 0) {
            LoginUserEntity loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
            // 更新缓存用户信息
            loginUser.getUser().setNickName(user.getNickName());
            loginUser.getUser().setPhonenumber(user.getPhonenumber());
            loginUser.getUser().setEmail(user.getEmail());
            loginUser.getUser().setSex(user.getSex());
            tokenService.setLoginUser(loginUser);
            return Result.success();
        }
        return Result.failed("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updatePwd")
    public Result updatePwd(@RequestBody UpdatePwdRequest updatePwdRequest) {
        // 解密用户密码
        String oldPassword = PasswordUtils.decryptPassword(updatePwdRequest.getOldPassword());
        String newPassword = PasswordUtils.decryptPassword(updatePwdRequest.getNewPassword());
        LoginUserEntity loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        String userName = loginUser.getUsername();
        String password = loginUser.getPassword();
        PasswordUtils.checkPassword(newPassword);
        if (StrUtil.isNotBlank(oldPassword) && !SecurityUtils.matchesPassword(oldPassword, password)) {
            return Result.failed("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, password)) {
            return Result.failed("新密码不能与旧密码相同");
        }
        if (userService.resetUserPwd(userName, SecurityUtils.encryptPassword(newPassword)) > 0) {
            // 更新缓存用户密码
            loginUser.getUser().setPassword(SecurityUtils.encryptPassword(newPassword));
            tokenService.setLoginUser(loginUser);
            // 记录状态 表示已经修改过密码
            loginUtils.updateChangedPasswordStatus(loginUser.getUser());
            return Result.success();
        }
        return Result.failed("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public Result avatar(@RequestParam("avatarfile") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String path = "avatar" + IdUtil.simpleUUID() + CharUtil.DOT + "png";
            String avatar = OssStorageFactory.getStorageService().upload(file.getInputStream(), path);
            LoginUserEntity loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
            if (userService.updateUserAvatar(loginUser.getUsername(), avatar)) {
                Map<String, Object> ajax = new HashMap<>();
                ajax.put("imgUrl", avatar);
                // 更新缓存用户头像
                loginUser.getUser().setAvatar(avatar);
                tokenService.setLoginUser(loginUser);
                return Result.success(ajax);
            }
        }
        return Result.failed("上传图片异常，请联系管理员");
    }


}



