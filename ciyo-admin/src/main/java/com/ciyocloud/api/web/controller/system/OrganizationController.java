package com.ciyocloud.api.web.controller.system;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.TreeUtils;
import com.ciyocloud.system.entity.SysDeptEntity;
import com.ciyocloud.system.entity.SysPostEntity;
import com.ciyocloud.system.entity.SysRoleEntity;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.request.SysRolePageReq;
import com.ciyocloud.system.request.SysUserPageReq;
import com.ciyocloud.system.service.SysDeptService;
import com.ciyocloud.system.service.SysPostService;
import com.ciyocloud.system.service.SysRoleService;
import com.ciyocloud.system.service.SysUserService;
import com.google.common.collect.ImmutableMap;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织架构
 * <p>相关查询接口 提供给选择部门人员 角色等查询
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/organization")
public class OrganizationController {
    private final SysDeptService deptService;
    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysPostService sysPostService;


    private String getDeptId(Long deptId) {
        if (ObjectUtil.isNull(deptId)) {
            deptId = 0L;
        }
        return "dept" + deptId;
    }


    /**
     * 查询全部部门数
     * 部门组件使用
     *
     * @return 部门树
     */
    @GetMapping("deptTrees")
    @PermitAll
    public Result<List<Map<String, Object>>> getDeptTrees(String formKey, Long formItemId) {
        List<SysDeptEntity> depts = deptService.listDept(new SysDeptEntity());
        depts = deptService.buildDeptTree(depts);
        // 转换下key名称
        List<Map<String, Object>> converted = TreeUtils.convertTreeList(depts, (key) -> {
            if (key.equals(SysDeptEntity.Fields.deptName)) {
                return "name";
            }
            return key;
        }, SysDeptEntity.Fields.children);
        InterceptorIgnoreHelper.clearIgnoreStrategy();
        return Result.success(converted);
    }


    /**
     * 查询全部部门以及下面员工树
     * 适用于用户量较小情况 用户量巨大情况请自行修改
     * 用户选择组件使用
     *
     * @return
     */
    @GetMapping("empTrees")
    @PermitAll
    public Result<List<Tree<String>>> getDeptEmployees(String formKey, Long formItemId) {
        List<SysDeptEntity> depts = deptService.listDept(new SysDeptEntity());
        handleDeptParentId(depts);
        List<SysUserEntity> users = sysUserService.listUsers(new SysUserPageReq());
        List<TreeNode<String>> nodeList = CollUtil.newArrayList();
        nodeList.addAll(depts.stream().map(item -> new TreeNode<>(getDeptId(item.getId()), getDeptId(item.getParentId()), item.getDeptName(), 5)).collect(Collectors.toList()));
        nodeList.addAll(users.stream().map(item -> {
            TreeNode<String> userNode = new TreeNode<>(item.getId().toString(), getDeptId(item.getDeptId()), item.getUserName(), 6);
            userNode.setExtra(ImmutableMap.of("nickName", item.getNickName()));
            return userNode;
        }).collect(Collectors.toList()));
        List<Tree<String>> treeList = TreeUtil.build(nodeList, getDeptId(0L));
        InterceptorIgnoreHelper.clearIgnoreStrategy();
        return Result.success(treeList);
    }

    /**
     * 部门处理
     */
    private void handleDeptParentId(List<SysDeptEntity> depts) {
        // 查找部门的父级id数据是否存在 不存在的全部把父级iD标记从0 避免构建树失败
        for (SysDeptEntity dept : depts) {
            if (dept.getParentId() != 0) {
                if (CollUtil.isEmpty(depts.stream().filter(item -> item.getId()
                        .equals(dept.getParentId())).collect(Collectors.toList()))) {
                    dept.setParentId(0L);
                }
            }
        }
    }


    /**
     * 分页角色列表
     */
    @GetMapping("/role/page")
    public Result queryRolePage(Page<SysRoleEntity> page, SysRoleEntity role) {
        return Result.success(sysRoleService.page(page, BeanUtil.copyProperties(role, SysRolePageReq.class)));
    }


    /**
     * 查询角色
     */
    @GetMapping("/role/list")
    public Result<List<SysRoleEntity>> getRoleList() {
        return Result.success(sysRoleService.listRoles(new SysRolePageReq()));
    }


    /**
     * 获取部门下拉树列表
     */
    @GetMapping("/dept/tree")
    public Result deptTree(SysDeptEntity dept) {
        List<SysDeptEntity> depts = deptService.listDept(dept);
        return Result.success(deptService.buildDeptTreeSelect(depts));
    }


    /**
     * 获取用户列表
     */
    @GetMapping("/user/page")
    public Result<Page<SysUserEntity>> queryPage(Page<SysUserEntity> page, SysUserEntity user) {
        Page<SysUserEntity> pageData = sysUserService.page(page, BeanUtil.copyProperties(user, SysUserPageReq.class));
        return Result.success(pageData);
    }


    /**
     * 获取岗位列表
     */
    @GetMapping("/post/page")
    public Result queryPostPage(Page page, SysPostEntity post) {
        return Result.success(sysPostService.page(page, QueryWrapperUtils.toSimpleQuery(post)));
    }

}
