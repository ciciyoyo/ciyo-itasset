package com.ciyocloud.api.security;

import cn.dev33.satoken.stp.StpInterface;
import com.ciyocloud.common.entity.security.LoginUserEntity;
import com.ciyocloud.common.entity.security.SysUserVO;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.system.service.SysMenuService;
import com.ciyocloud.system.service.SysRoleService;
import com.ciyocloud.system.service.security.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sa-Token 权限认证接口实现
 * 
 * @author codeck
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    
    private final SysPermissionService permissionService;

    /**
     * 返回指定账号id所拥有的权限码集合
     * 
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        try {
            LoginUserEntity loginUser = SecurityUtils.getLoginUserNotEx();
            if (loginUser == null || loginUser.getUser() == null) {
                return new ArrayList<>();
            }
            
            SysUserVO user = loginUser.getUser();
            
            // 超级管理员拥有所有权限
            if (SecurityUtils.isAdmin(user.getId())) {
                List<String> list = new ArrayList<>();
                list.add("*");
                return list;
            }
            
            Set<String> permissions = permissionService.getMenuPermission(user);
            return new ArrayList<>(permissions);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 返回指定账号id所拥有的角色标识集合
     * 
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            LoginUserEntity loginUser = SecurityUtils.getLoginUserNotEx();
            if (loginUser == null || loginUser.getUser() == null) {
                return new ArrayList<>();
            }
            
            SysUserVO user = loginUser.getUser();
            Set<String> roles = permissionService.getRolePermission(user);
            return new ArrayList<>(roles);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
