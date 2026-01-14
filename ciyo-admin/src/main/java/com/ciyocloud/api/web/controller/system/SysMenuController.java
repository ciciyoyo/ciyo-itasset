package com.ciyocloud.api.web.controller.system;

import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysMenuEntity;
import com.ciyocloud.system.service.SysMenuService;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 菜单信息
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/menu")
public class SysMenuController {
    private final SysMenuService menuService;

    /**
     * 获取菜单列表
     */
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping("/list")
    public Result list(SysMenuEntity menu) {
        Long userId = SecurityUtils.getUserId();
        List<SysMenuEntity> menus = menuService.getMenuList(menu, userId);
        return Result.success(menus);
    }

    /**
     * 根据菜单编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    public Result getInfo(@PathVariable Long menuId) {
        return Result.success(menuService.getById(menuId));
    }

    /**
     * 获取菜单下拉树列表
     */
    @GetMapping("/treeselect")
    public Result treeselect(SysMenuEntity menu) {
        Long userId = SecurityUtils.getUserId();
        List<SysMenuEntity> menus = menuService.getMenuList(menu, userId);
        return Result.success(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 获取菜单树
     */
    @GetMapping("/getMenuTreeAll")
    public Result getMenuTreeAll(SysMenuEntity menu) {
        List<SysMenuEntity> menus = menuService.getMenuTreeAll();
        return Result.success(menuService.buildMenusEntity(menus));
    }


    /**
     * 加载对应角色菜单列表树
     */
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    public Result roleMenuTreeselect(@PathVariable("roleId") Long roleId) {
        Long userId = SecurityUtils.getUserId();
        List<SysMenuEntity> menus = menuService.getMenuList(userId);
        Map<String, Object> result = Maps.newHashMap();
        result.put("checkedKeys", menuService.getMenuListByRoleId(roleId));
        result.put("menus", menuService.buildMenuTreeSelect(menus));
        return Result.success(result);
    }

    /**
     * 新增菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public Result save(@Validated @RequestBody SysMenuEntity menu) {
        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
            return Result.failed("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame())
                && !StringUtils.startsWithAny(menu.getPath(), "http", "https")) {
            return Result.failed("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        }
        menu.setCreateBy(SecurityUtils.getUserId());
        return Result.success(menuService.save(menu));
    }

    /**
     * 修改菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result update(@Validated @RequestBody SysMenuEntity menu) {
        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
            return Result.failed("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame())
                && !StringUtils.startsWithAny(menu.getPath(), "http", "https")) {
            return Result.failed("修改菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        } else if (menu.getId().equals(menu.getParentId())) {
            return Result.failed("修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
        }
        menu.setUpdateBy(SecurityUtils.getUserId());
        return Result.success(menuService.updateById(menu));
    }

    /**
     * 删除菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public Result delete(@PathVariable("menuId") Long menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            return Result.failed("存在子菜单,不允许删除");
        }
        if (menuService.checkMenuExistRole(menuId)) {
            return Result.failed("菜单已分配,不允许删除");
        }
        return Result.success(menuService.removeById(menuId));
    }
}
