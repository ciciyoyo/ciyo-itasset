package com.ciyocloud.api.web.controller.system;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import com.ciyocloud.common.entity.security.LoginUserEntity;
import com.ciyocloud.common.entity.security.SysUserVO;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import com.ciyocloud.system.entity.SysMenuEntity;
import com.ciyocloud.system.request.LoginRequest;
import com.ciyocloud.system.service.SysMenuService;
import com.ciyocloud.system.service.security.SysLoginService;
import com.ciyocloud.system.service.security.SysPermissionService;
import com.ciyocloud.system.util.PasswordUtils;
import com.ciyocloud.system.vo.SysUserLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 登录验证
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
public class SysLoginController {
    private final SysLoginService loginService;

    private final SysMenuService menuService;

    private final SysPermissionService permissionService;


    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    @Log(title = "账号登录", businessType = BusinessType.LOGIN, accountEl = "#loginBody.username", isSaveResponseData = false)
    public Result<SysUserLoginVO> login(@RequestBody LoginRequest loginBody) {
        // 生成令牌
        LoginUserEntity loginUser = loginService.login(loginBody.getUsername(), PasswordUtils.decryptPassword(loginBody.getPassword()));
        
        // 登录，使用用户ID作为登录标识
        StpUtil.login(loginUser.getUser().getId());
        
        // 将用户信息存入 Session
        SecurityUtils.setLoginUser(loginUser);
        
        // 获取 Token
        String token = StpUtil.getTokenValue();
        loginUser.setToken(token);
        
        boolean needChangePassword = loginService.isNeedChangePassword(loginUser.getUser());
        return Result.success(SysUserLoginVO.builder()
                .token(token).needChangePassword(needChangePassword).build());
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @Log(title = "退出登录", businessType = BusinessType.LOGOUT)
    public Result logout() {
        StpUtil.logout();
        return Result.success("退出成功");
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public Result getInfo() {
        LoginUserEntity loginUser = (LoginUserEntity) StpUtil.getSession().get("loginUser");
        SysUserVO user = loginUser.getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        Map<String, Object> result = MapUtil.newHashMap();
        result.put("user", user);
        result.put("roles", roles);
        result.put("permissions", permissions);
        return Result.success(result);
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public Result getRouters() {
        LoginUserEntity loginUser = (LoginUserEntity) StpUtil.getSession().get("loginUser");
        // 用户信息
        SysUserVO user = loginUser.getUser();
        List<SysMenuEntity> menus = menuService.getMenuTreeByUserId(user.getId());
        return Result.success(menuService.buildMenus(menus));
    }
}
