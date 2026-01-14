package com.ciyocloud.api.web.controller.system;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysDeptEntity;
import com.ciyocloud.system.service.SysDeptService;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 部门信息
 *
 * @author codeck
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dept")
public class SysDeptController {
    private final SysDeptService deptService;

    /**
     * 获取部门列表
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/page")
    public Result queryPage(SysDeptEntity dept) {
        return Result.success(deptService.listDept(dept));
    }

    /**
     * 获取部门列表
     */
    @GetMapping("/list")
    public Result list(@RequestParam("deptIds") List<Long> deptIds) {
        return Result.success(deptService.list(Wrappers.<SysDeptEntity>lambdaQuery().in(SysDeptEntity::getId, deptIds)));
    }

    /**
     * 查询部门列表（排除节点）
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list/exclude/{deptId}")
    public Result excludeChild(@PathVariable(value = "deptId", required = false) Long deptId) {
        List<SysDeptEntity> depts = deptService.list();
        Iterator<SysDeptEntity> it = depts.iterator();
        while (it.hasNext()) {
            SysDeptEntity d = it.next();
            if (d.getId().intValue() == deptId
                    || CollectionUtil.contains(StrUtil.split(d.getAncestors(), ","), deptId + "")) {
                it.remove();
            }
        }
        return Result.success(depts);
    }

    /**
     * 根据部门编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @GetMapping(value = "/{deptId}")
    public Result getInfo(@PathVariable Long deptId) {
        return Result.success(deptService.getById(deptId));
    }

    /**
     * 获取部门下拉树列表
     */
    @GetMapping("/treeselect")
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    public Result treeselect(SysDeptEntity dept) {
        List<SysDeptEntity> depts = deptService.listDept(dept);
        return Result.success(deptService.buildDeptTreeSelect(depts));
    }

    /**
     * 加载对应角色部门列表树
     */
    @GetMapping(value = "/roleDeptTreeselect/{roleId}")
    public Result roleDeptTreeselect(@PathVariable("roleId") Long roleId) {
        List<SysDeptEntity> depts = deptService.list();
        Map result = Maps.newHashMap();
        result.put("checkedKeys", deptService.getDeptListByRoleId(roleId));
        result.put("depts", deptService.buildDeptTreeSelect(depts));
        return Result.success(result);
    }

    /**
     * 新增部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public Result save(@Validated @RequestBody SysDeptEntity dept) {
        if (UserConstants.NOT_UNIQUE.equals(deptService.checkDeptNameUnique(dept))) {
            return Result.failed("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        dept.addInit();
        dept.setCreateBy(SecurityUtils.getUserId());
        return Result.success(deptService.saveDept(dept));
    }

    /**
     * 修改部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result update(@Validated @RequestBody SysDeptEntity dept) {
        if (UserConstants.NOT_UNIQUE.equals(deptService.checkDeptNameUnique(dept))) {
            return Result.failed("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        } else if (dept.getParentId().equals(dept.getId())) {
            return Result.failed("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        } else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus())
                && deptService.getNormalChildrenDeptById(dept.getId()) > 0) {
            return Result.failed("该部门包含未停用的子部门！");
        }
        dept.setUpdateBy(SecurityUtils.getUserId());
        return Result.success(deptService.updateDept(dept));
    }

    /**
     * 删除部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public Result delete(@PathVariable Long deptId) {
        if (deptService.hasChildByDeptId(deptId)) {
            return Result.failed("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId)) {
            return Result.failed("部门存在用户,不允许删除");
        }
        return Result.success(deptService.removeById(deptId));
    }
}
