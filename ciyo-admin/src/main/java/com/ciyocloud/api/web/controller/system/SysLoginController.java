package com.ciyocloud.api.web.controller.system;

import cn.hutool.core.map.MapUtil;
import com.ciyocloud.common.entity.security.LoginUserEntity;
import com.ciyocloud.common.entity.security.SysUserVO;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import com.ciyocloud.system.entity.SysMenuEntity;
import com.ciyocloud.system.request.LoginRequest;
import com.ciyocloud.system.service.SysMenuService;
import com.ciyocloud.system.service.security.SysLoginService;
import com.ciyocloud.system.service.security.SysPermissionService;
import com.ciyocloud.system.service.security.TokenService;
import com.ciyocloud.system.util.PasswordUtils;
import com.ciyocloud.system.vo.SysUserLoginVO;
import jakarta.servlet.http.HttpServletRequest;
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

    private final TokenService tokenService;


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
        LoginUserEntity login = loginService.login(loginBody.getUsername(), PasswordUtils.decryptPassword(loginBody.getPassword()));
        boolean needChangePassword = loginService.isNeedChangePassword(login.getUser());
        return Result.success(SysUserLoginVO.builder()
                .token(tokenService.createToken(login)).needChangePassword(needChangePassword).build());
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public Result getInfo(HttpServletRequest request) {
        LoginUserEntity loginUser = tokenService.getLoginUser(request);
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
    public Result getRouters(HttpServletRequest request) {
        LoginUserEntity loginUser = tokenService.getLoginUser(request);
        // 用户信息
        SysUserVO user = loginUser.getUser();
        List<SysMenuEntity> menus = menuService.getMenuTreeByUserId(user.getId());
        return Result.success(menuService.buildMenus(menus));
    }
}
