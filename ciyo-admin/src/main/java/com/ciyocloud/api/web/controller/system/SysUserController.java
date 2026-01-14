package com.ciyocloud.api.web.controller.system;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.excel.core.ExcelResult;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysRoleEntity;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.listener.SysUserImportListener;
import com.ciyocloud.system.request.SysRolePageReq;
import com.ciyocloud.system.request.SysUserPageReq;
import com.ciyocloud.system.service.SysDeptPostService;
import com.ciyocloud.system.service.SysRoleService;
import com.ciyocloud.system.service.SysUserService;
import com.ciyocloud.system.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户信息
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
public class SysUserController {
    private final SysUserService userService;

    private final SysRoleService roleService;

    private final SysDeptPostService postService;

    /**
     * 获取用户列表
     */
    @GetMapping("/page")
    @SaCheckPermission("system:user:list")
    public Result<Page<SysUserEntity>> queryPage(Page<SysUserEntity> page, SysUserPageReq user) {
        Page<SysUserEntity> pageData = userService.page(page, user);
        return Result.success(pageData);
    }


    /**
     * 导出用户列表
     */
    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("system:user:export")
    @GetMapping("/export")
    public void export(SysUserPageReq user) {
        List<SysUserEntity> list = userService.listUsers(user);
        ExcelUtils.exportExcel(list, "用户数据", SysUserEntity.class);
    }

    /**
     * 导入用户列表
     */
    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @SaCheckPermission("system:user:import")
    @PostMapping("/importData")
    public Result importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelResult<SysUserEntity> result = ExcelUtils.importExcel(file.getInputStream(), SysUserEntity.class, new SysUserImportListener(updateSupport));
        return Result.success(null, result.getAnalysis());
    }

    /**
     * 下载导入用户数据模板
     */
    @GetMapping("/importTemplate")
    public void importTemplate() {
        ExcelUtils.downloadTemplate("用户导入模板", "/template/userImportTemplate.xlsx");
    }

    /**
     * 根据用户编号获取详细信息
     */
    @SaCheckPermission("system:user:query")
    @GetMapping(value = {"/", "/{userId}"})
    public Result getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        userService.checkUserDataScope(userId);
        SysRolePageReq req = new SysRolePageReq();
        List<SysRoleEntity> roles = roleService.listRoles(req);
        result.put("roles", SysUserEntity.isAdmin(userId) ?
                roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        if (ObjectUtil.isNotNull(userId)) {
            result.put("user", userService.getById(userId));
            result.put("postIds", postService.getDeptPostListByUserId(userId));
            result.put("roleIds", roleService.getRoleIdListByUserId(userId));
        }
        return Result.success(result);
    }

    /**
     * 新增用户
     */
    @SaCheckPermission("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public Result save(@Validated @RequestBody SysUserEntity user) {
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user.getUserName()))) {
            return Result.failed("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StrUtil.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return Result.failed("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StrUtil.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return Result.failed("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setCreateBy(SecurityUtils.getUserId());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return Result.success(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result update(@Validated @RequestBody SysUserEntity user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getId());
        if (StrUtil.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return Result.failed("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StrUtil.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return Result.failed("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(SecurityUtils.getUserId());
        return Result.success(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @SaCheckPermission("system:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public Result delete(@PathVariable List<Long> userIds) {
        return Result.success(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @SaCheckPermission("system:user:resetPwd")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public Result resetPwd(@RequestBody SysUserEntity user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getId());
        PasswordUtils.checkPassword(user.getPassword());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(SecurityUtils.getUserId());
        return Result.success(userService.resetPwd(user));
    }

    /**
     * 状态修改
     */
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public Result changeStatus(@RequestBody SysUserEntity user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getId());
        user.setUpdateBy(SecurityUtils.getUserId());
        return Result.success(userService.updateUserStatus(user));
    }

    /**
     * 根据用户编号获取授权角色
     */
    @SaCheckPermission("system:user:query")
    @GetMapping("/authRole/{userId}")
    public Result authRole(@PathVariable("userId") Long userId) {
        Map<String, Object> ajax = new HashMap<>();
        SysUserEntity user = userService.getById(userId);
        List<SysRoleEntity> roles = roleService.getRolesFlagByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", SysUserEntity.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return Result.success(ajax);
    }

    /**
     * 用户授权角色
     */
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public Result insertAuthRole(Long userId, @RequestParam("roleIds") List<Long> roleIds) {
        userService.checkUserDataScope(userId);
        userService.insertUserAuth(userId, roleIds);
        return Result.success();
    }
}
