package com.ciyocloud.system.service.security;

import com.ciyocloud.common.entity.security.SysUserVO;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.system.service.SysMenuService;
import com.ciyocloud.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户权限处理
 *
 * @author codeck
 */
@Component
@RequiredArgsConstructor
public class SysPermissionService {
    private final SysRoleService roleService;

    private final SysMenuService menuService;

    /**
     * 获取角色数据权限
     *
     * @param user 用户信息
     * @return 角色权限信息
     */
    public Set<String> getRolePermission(SysUserVO user) {
        Set<String> roles = new HashSet<String>();
        // 管理员拥有所有权限
        if (SecurityUtils.isAdmin(user.getId())) {
            roles.add("admin");
        } else {
            roles.addAll(roleService.getRolePermissionByUserId(user.getId()));
        }
        return roles;
    }

    /**
     * 获取菜单数据权限
     *
     * @param user 用户信息
     * @return 菜单权限信息
     */
    public Set<String> getMenuPermission(SysUserVO user) {
        Set<String> perms = new HashSet<String>();
        // 管理员拥有所有权限
        if (SecurityUtils.isAdmin(user.getId())) {
            perms.add("*:*:*");
        } else {
            perms.addAll(menuService.getMenuPermsByUserId(user.getId()));
        }
        return perms;
    }
}
