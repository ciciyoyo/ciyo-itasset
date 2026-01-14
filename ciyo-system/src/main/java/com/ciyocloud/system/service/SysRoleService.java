package com.ciyocloud.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.system.entity.SysRoleEntity;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.request.SysRolePageReq;

import java.util.List;
import java.util.Set;

/**
 * 角色业务层
 *
 * @author codeck
 */
public interface SysRoleService extends IService<SysRoleEntity> {

    /**
     * 分页查询
     *
     * @param page
     * @param role
     * @return
     */
    Page page(Page page, SysRolePageReq role);


    /**
     * 列表查询数据权限
     *
     * @param role
     * @return
     */
    List<SysRoleEntity> listRoles(SysRolePageReq role);

    /**
     * 查询所有角色及当前用户是否拥有状态
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRoleEntity> getRolesFlagByUserId(Long userId);

    /**
     * 根据用户ID查询角色权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    Set<String> getRolePermissionByUserId(Long userId);


    /**
     * 根据用户ID获取角色选择框列表
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    List<Integer> getRoleIdListByUserId(Long userId);


    /**
     * 校验角色名称是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    String checkRoleNameUnique(SysRoleEntity role);

    /**
     * 校验角色权限是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    String checkRoleKeyUnique(SysRoleEntity role);

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    void checkRoleAllowed(SysRoleEntity role);

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    Long countUserRoleByRoleId(Long roleId);

    /**
     * 新增保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    int saveRole(SysRoleEntity role);

    /**
     * 修改保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    int updateRole(SysRoleEntity role);

    /**
     * 修改角色状态
     *
     * @param role 角色信息
     * @return 结果
     */
    int updateRoleStatus(SysRoleEntity role);

    /**
     * 修改数据权限信息
     *
     * @param role 角色信息
     * @return 结果
     */
    int authDataScope(SysRoleEntity role);


    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    int deleteRoleById(Long roleId);

    /**
     * 批量删除角色信息
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    int deleteRoleByIds(List<Long> roleIds);

    /***
     * 获取角色用户列表
     */
    List<SysUserEntity> getRoleUserList(List<Long> roleIdList);


}
