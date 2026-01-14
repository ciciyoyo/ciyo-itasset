package com.ciyocloud.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.datapermission.annotation.DataColumn;
import com.ciyocloud.datapermission.annotation.DataPermission;
import com.ciyocloud.system.entity.SysRoleEntity;
import com.ciyocloud.system.entity.SysUserEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色表 数据层
 *
 * @author codeck
 */
public interface SysRoleMapper extends BaseMapper<SysRoleEntity> {


    /**
     * 分页查询角色
     *
     * @param page
     * @param queryWrapper
     * @return
     */
    @DataPermission({
            @DataColumn(key = "deptName", value = "d.id"),
            @DataColumn(key = "userName", value = "u.id")
    })
    Page<SysRoleEntity> selectRolePage(@Param("page") Page<SysRoleEntity> page, @Param(Constants.WRAPPER) Wrapper<SysRoleEntity> queryWrapper);


    @DataPermission({
            @DataColumn(key = "deptName", value = "d.id"),
            @DataColumn(key = "userName", value = "u.id")
    })
    List<SysRoleEntity> selectRoleList(@Param(Constants.WRAPPER) Wrapper<SysRoleEntity> queryWrapper);

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRoleEntity> selectRolePermissionByUserId(Long userId);


    /**
     * 根据用户ID获取角色选择框列表
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    List<Integer> selectRoleIdListByUserId(Long userId);


    /**
     * 根据用户ID查询角色
     *
     * @param userName 用户名
     * @return 角色列表
     */
    List<SysRoleEntity> selectRolesByUserName(String userName);

    /**
     * 校验角色名称是否唯一
     *
     * @param roleName 角色名称
     * @return 角色信息
     */
    SysRoleEntity checkRoleNameUnique(String roleName);

    /**
     * 校验角色权限是否唯一
     *
     * @param roleKey 角色权限
     * @return 角色信息
     */
    SysRoleEntity checkRoleKeyUnique(String roleKey);


    /**
     * 查询角色下的用户
     */
    List<SysUserEntity> selectRoleUserList(@Param("roleIdList") List<Long> roleIdList);


}
