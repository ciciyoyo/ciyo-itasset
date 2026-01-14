package com.ciyocloud.api.web.controller.system;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.entity.security.LoginUserEntity;
import com.ciyocloud.common.entity.security.SysUserVO;
import com.ciyocloud.common.util.*;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysRoleEntity;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.request.SysRolePageReq;
import com.ciyocloud.system.service.SysRoleService;
import com.ciyocloud.system.service.SysUserService;
import com.ciyocloud.system.service.security.SysPermissionService;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 角色信息
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/role")
public class SysRoleController {
    private final SysRoleService roleService;

    private final SysPermissionService permissionService;

    private final SysUserService userService;


    /**
     * 分页角色列表
     */
    @SaCheckPermission("system:role:list")
    @GetMapping("/page")
    public Result queryPage(Page page, SysRolePageReq role) {
        return Result.success(roleService.page(page, role));
    }


    /**
     * 获取角色列表
     */
    @GetMapping("/list")
    @SaCheckPermission("system:role:list")
    public Result<List<SysRoleEntity>> list() {
        return Result.success(roleService.listRoles(new SysRolePageReq()));
    }


    /**
     * 导出角色列表
     */
    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("system:role:export")
    @GetMapping("/export")
    public void export(SysRolePageReq role) {
        List<SysRoleEntity> list = roleService.list(QueryWrapperUtils.toSimpleQuery(role, SysRoleEntity.class));
        ExcelUtils.exportExcel(list, "角色数据", SysRoleEntity.class);
    }

    /**
     * 根据角色编号获取详细信息
     */
    @SaCheckPermission("system:role:query")
    @GetMapping(value = "/{roleId}")
    public Result getInfo(@PathVariable Long roleId) {
        return Result.success(roleService.getById(roleId));
    }

    /**
     * 新增角色
     */
    @SaCheckPermission("system:role:add")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public Result save(@Validated @RequestBody SysRoleEntity role) {
        if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role))) {
            return Result.failed("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleKeyUnique(role))) {
            return Result.failed("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.addInit();
        role.setCreateBy(SecurityUtils.getUserId());
        int i = roleService.saveRole(role);
        if (i > 0) {
            // 添加角色后给当前用户加一个 不然数据权限原因会导致他自己看不到这个角色
            Long userId = SecurityUtils.getUserId();
            userService.assignRoleToUser(userId, role.getId());
        }
        return Result.success();

    }

    /**
     * 修改保存角色
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result update(@Validated @RequestBody SysRoleEntity role) {
        roleService.checkRoleAllowed(role);
        if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role))) {
            return Result.failed("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleKeyUnique(role))) {
            return Result.failed("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setUpdateBy(SecurityUtils.getUserId());

        if (roleService.updateRole(role) > 0) {
            // 更新缓存用户权限
            LoginUserEntity loginUser = SecurityUtils.getLoginUser();
            if (ObjectUtil.isNotNull(loginUser.getUser()) && !loginUser.getUser().isAdmin()) {
                loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
                SysUserEntity user = userService.getUserByUserName(loginUser.getUser().getUserName());
                SysUserVO sysUserVO = JsonUtils.objToObj(user, SysUserVO.class);
                loginUser.setUser(sysUserVO);
                SecurityUtils.setLoginUser(loginUser);
            }
            return Result.success();
        }
        return Result.failed("修改角色'" + role.getRoleName() + "'失败，请联系管理员");
    }

    /**
     * 修改保存数据权限
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/dataScope")
    public Result dataScope(@RequestBody SysRoleEntity role) {
        roleService.checkRoleAllowed(role);
        return Result.success(roleService.authDataScope(role));
    }

    /**
     * 状态修改
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public Result changeStatus(@RequestBody SysRoleEntity role) {
        roleService.checkRoleAllowed(role);
        role.setUpdateBy(SecurityUtils.getUserId());
        return Result.success(roleService.updateRoleStatus(role));
    }

    /**
     * 删除角色
     */
    @SaCheckPermission("system:role:remove")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public Result delete(@PathVariable List<Long> roleIds) {
        return Result.success(roleService.deleteRoleByIds(roleIds));
    }

    /**
     * 获取角色选择框列表
     */
    @SaCheckPermission("system:role:query")
    @GetMapping("/optionselect")
    public Result optionselect() {
        return Result.success(roleService.list(Wrappers.<SysRoleEntity>lambdaQuery().eq(SysRoleEntity::getStatus, "0")));
    }
}
