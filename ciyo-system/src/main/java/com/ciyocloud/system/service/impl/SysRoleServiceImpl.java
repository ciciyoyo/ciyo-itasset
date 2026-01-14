package com.ciyocloud.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.exception.BaseException;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.system.constant.SystemConstants;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.*;
import com.ciyocloud.system.mapper.SysRoleDeptMapper;
import com.ciyocloud.system.mapper.SysRoleMapper;
import com.ciyocloud.system.mapper.SysRoleMenuMapper;
import com.ciyocloud.system.mapper.SysUserRoleMapper;
import com.ciyocloud.system.request.SysRolePageReq;
import com.ciyocloud.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 角色 业务层处理
 *
 * @author codeck
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {
    private final SysRoleMapper roleMapper;

    private final SysRoleMenuMapper roleMenuMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final SysRoleDeptMapper roleDeptMapper;


    @Override
    public Page page(Page page, SysRolePageReq role) {
        return roleMapper.selectRolePage(page, this.buildQueryWrapper(role));
    }


    private Wrapper<SysRoleEntity> buildQueryWrapper(SysRolePageReq bo) {
        QueryWrapper<SysRoleEntity> wrapper = Wrappers.query();
        wrapper.eq("r.del_flag", SystemConstants.NORMAL)
                .eq(ObjectUtil.isNotNull(bo.getId()), "r.id", bo.getId())
                .like(StringUtils.isNotBlank(bo.getRoleName()), "r.role_name", bo.getRoleName())
                .eq(StringUtils.isNotBlank(bo.getStatus()), "r.status", bo.getStatus())
                .like(StringUtils.isNotBlank(bo.getRoleKey()), "r.role_key", bo.getRoleKey())
                .orderByAsc("r.role_sort").orderByAsc("r.create_time");
        QueryWrapperUtils.addDateTimeRange("r.create_time", bo.getParams(), wrapper);
        return wrapper;
    }

    @Override
    public List<SysRoleEntity> listRoles(SysRolePageReq role) {
        return roleMapper.selectRoleList(this.buildQueryWrapper(role));
    }

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<SysRoleEntity> getRolesFlagByUserId(Long userId) {
        List<SysRoleEntity> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        List<SysRoleEntity> roles = this.list(Wrappers.lambdaQuery(SysRoleEntity.class).eq(SysRoleEntity::getStatus, SystemConstants.NORMAL));
        for (SysRoleEntity role : roles) {
            for (SysRoleEntity userRole : userRoles) {
                if (role.getId().longValue() == userRole.getId().longValue()) {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> getRolePermissionByUserId(Long userId) {
        List<SysRoleEntity> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRoleEntity perm : perms) {
            if (ObjectUtil.isNotNull(perm)) {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }


    /**
     * 根据用户ID获取角色选择框列表
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    @Override
    public List<Integer> getRoleIdListByUserId(Long userId) {
        return roleMapper.selectRoleIdListByUserId(userId);
    }


    /**
     * 校验角色名称是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public String checkRoleNameUnique(SysRoleEntity role) {
        Long roleId = ObjectUtil.isNull(role.getId()) ? -1L : role.getId();
        SysRoleEntity info = roleMapper.checkRoleNameUnique(role.getRoleName());
        if (ObjectUtil.isNotNull(info) && info.getId().longValue() != roleId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色权限是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public String checkRoleKeyUnique(SysRoleEntity role) {
        Long roleId = ObjectUtil.isNull(role.getId()) ? -1L : role.getId();
        SysRoleEntity info = roleMapper.checkRoleKeyUnique(role.getRoleKey());
        if (ObjectUtil.isNotNull(info) && info.getId().longValue() != roleId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    @Override
    public void checkRoleAllowed(SysRoleEntity role) {
        if (ObjectUtil.isNotNull(role.getId()) && role.isAdmin()) {
            throw new BaseException("不允许操作超级管理员角色");
        }
    }

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public Long countUserRoleByRoleId(Long roleId) {
        return userRoleMapper.selectCount(Wrappers.<SysUserRoleEntity>lambdaQuery().eq(SysUserRoleEntity::getRoleId, roleId));
    }

    /**
     * 新增保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveRole(SysRoleEntity role) {
        // 新增角色信息
        roleMapper.insert(role);
        return insertRoleMenu(role);
    }

    /**
     * 修改保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRole(SysRoleEntity role) {
        // 修改角色信息
        roleMapper.updateById(role);
        // 删除角色与菜单关联
        roleMenuMapper.delete(Wrappers.<SysRoleMenuEntity>lambdaQuery().eq(SysRoleMenuEntity::getRoleId, role.getId()));
        return insertRoleMenu(role);
    }

    /**
     * 修改角色状态
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public int updateRoleStatus(SysRoleEntity role) {
        return roleMapper.updateById(role);
    }

    /**
     * 修改数据权限信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int authDataScope(SysRoleEntity role) {
        // 修改角色信息
        roleMapper.updateById(role);
        // 删除角色与部门关联
        roleDeptMapper.delete(Wrappers.<SysRoleDeptEntity>lambdaQuery().eq(SysRoleDeptEntity::getRoleId, role.getId()));
        // 新增角色和部门信息（数据权限）
        return insertRoleDept(role);
    }

    /**
     * 新增角色菜单信息
     *
     * @param role 角色对象
     */
    public int insertRoleMenu(SysRoleEntity role) {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenuEntity> list = new ArrayList<SysRoleMenuEntity>();
        for (Long menuId : role.getMenuIds()) {
            SysRoleMenuEntity rm = new SysRoleMenuEntity();
            rm.setRoleId(role.getId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (!list.isEmpty()) {
            list.forEach(roleMenuMapper::insert);
        }
        return rows;
    }

    /**
     * 新增角色部门信息(数据权限)
     *
     * @param role 角色对象
     */
    public int insertRoleDept(SysRoleEntity role) {
        int rows = 1;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDeptEntity> list = new ArrayList<SysRoleDeptEntity>();
        for (Long deptId : role.getDeptIds()) {
            SysRoleDeptEntity rd = new SysRoleDeptEntity();
            rd.setRoleId(role.getId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (!list.isEmpty()) {
            list.forEach(roleDeptMapper::insert);
        }
        return rows;
    }

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleById(Long roleId) {
        // 删除角色与菜单关联
        roleMenuMapper.delete(Wrappers.<SysRoleMenuEntity>lambdaQuery().eq(SysRoleMenuEntity::getRoleId, roleId));
        // 删除角色与部门关联
        roleDeptMapper.delete(Wrappers.<SysRoleDeptEntity>lambdaQuery().eq(SysRoleDeptEntity::getRoleId, roleId));
        return roleMapper.deleteById(roleId);
    }

    /**
     * 批量删除角色信息
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleByIds(List<Long> roleIds) {
        for (Long roleId : roleIds) {
            checkRoleAllowed(new SysRoleEntity(roleId));
            SysRoleEntity role = this.getById(roleId);
            if (countUserRoleByRoleId(roleId) > 0) {
                throw new BaseException(String.format("%1$s已分配,不能删除", role.getRoleName()));
            }
        }
        // 删除角色与菜单关联
        roleMenuMapper.delete(Wrappers.<SysRoleMenuEntity>lambdaQuery().in(SysRoleMenuEntity::getRoleId, roleIds));
        // 删除角色与部门关联
        roleDeptMapper.delete(Wrappers.<SysRoleDeptEntity>lambdaQuery().in(SysRoleDeptEntity::getRoleId, roleIds));
        return roleMapper.deleteBatchIds(roleIds);
    }

    @Override
    public List<SysUserEntity> getRoleUserList(List<Long> roleIdList) {
        return roleMapper.selectRoleUserList(roleIdList);
    }


}
