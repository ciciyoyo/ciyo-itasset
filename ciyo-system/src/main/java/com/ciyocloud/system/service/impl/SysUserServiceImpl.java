package com.ciyocloud.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.exception.BaseException;
import com.ciyocloud.common.mybatis.service.BaseServiceImpl;
import com.ciyocloud.common.util.*;
import com.ciyocloud.system.constant.SysConfigConstants;
import com.ciyocloud.system.constant.SystemConstants;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.*;
import com.ciyocloud.system.mapper.*;
import com.ciyocloud.system.request.SysUserPageReq;
import com.ciyocloud.system.service.SysConfigService;
import com.ciyocloud.system.service.SysDeptService;
import com.ciyocloud.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ciyocloud.system.constant.SysConfigConstants.REGISTER_ROLE;


/**
 * 用户 业务层处理
 *
 * @author codeck
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends BaseServiceImpl<SysUserMapper, SysUserEntity> implements SysUserService {

    private final SysRoleMapper roleMapper;

    private final SysPostMapper postMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final SysUserPostMapper userPostMapper;

    private final SysDeptService sysDeptService;

    private final SysConfigService configService;

    @Override
    public Page<SysUserEntity> page(Page<SysUserEntity> page, SysUserPageReq user) {
        return baseMapper.selectUserPage(page, this.buildQueryWrapper(user));
    }


    private Wrapper<SysUserEntity> buildQueryWrapper(SysUserPageReq user) {
        QueryWrapper<SysUserEntity> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", SystemConstants.NORMAL).eq(ObjectUtil.isNotNull(user.getId()), "u.id", user.getId())
                .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
                .like(StringUtils.isNotBlank(user.getNickName()), "u.nick_name", user.getNickName())
                .eq(StringUtils.isNotBlank(user.getStatus()), "u.status", user.getStatus())
                .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber()).like(StringUtils.isNotBlank(user.getEmail()), "u.email", user.getEmail())
                .and(ObjectUtil.isNotNull(user.getDeptId()), w -> {
                    // 部门查询支持查询子部门
                    List<Long> ids = sysDeptService.getChildrenDeptIdList(user.getDeptId());
                    ids.add(user.getDeptId());
                    w.in("u.dept_id", ids);
                }).and(CollUtil.isNotEmpty(user.getRoleIds()), w -> {
                    List<SysUserRoleEntity> roleEntityList = userRoleMapper.selectList(Wrappers.<SysUserRoleEntity>lambdaQuery().select(SysUserRoleEntity::getUserId).in(SysUserRoleEntity::getRoleId, user.getRoleIds()));
                    List<Long> ids = StreamUtils.toList(roleEntityList, SysUserRoleEntity::getUserId);
                    ids.add(0L);
                    //避免报错
                    w.in("u.id", ids);
                }).orderByAsc("u.id");
        QueryWrapperUtils.addDateTimeRange("u.create_time", user.getParams(), wrapper);
        return wrapper;
    }


    /**
     * 查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUserEntity> listUsers(SysUserPageReq user) {
        return baseMapper.selectUserList(this.buildQueryWrapper(user));
    }

    @Override
    public List<Long> listAuthorizedUserIds() {
        return baseMapper.selectAuthorizedUserIds();
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUserEntity getUserByUserName(String userName) {
        return baseMapper.selectUserByUserName(userName);
    }

    /**
     * 通过Id查询用户
     *
     * @param userId id
     * @return 用户对象信息
     */
    @Override
    public SysUserEntity getUserByUserId(Long userId) {
        return baseMapper.selectUserById(userId);
    }


    /**
     * 查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String getUserRoleGroup(String userName) {
        List<SysRoleEntity> list = roleMapper.selectRolesByUserName(userName);
        StringBuffer idsStr = new StringBuffer();
        for (SysRoleEntity role : list) {
            idsStr.append(role.getRoleName()).append(",");
        }
        if (StringUtils.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String getUserPostGroup(String userName) {
        List<SysPostEntity> list = postMapper.selectPostsByUserName(userName);
        StringBuffer idsStr = new StringBuffer();
        for (SysPostEntity post : list) {
            idsStr.append(post.getPostName()).append(",");
        }
        if (StringUtils.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param userName 用户名称
     * @return 结果
     */
    @Override
    public String checkUserNameUnique(String userName) {
        int count = baseMapper.checkUserNameUnique(userName);
        if (count > 0) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public String checkPhoneUnique(SysUserEntity user) {
        Long userId = ObjectUtil.isNull(user.getId()) ? -1L : user.getId();
        SysUserEntity info = baseMapper.checkPhoneUnique(user.getPhonenumber());
        if (ObjectUtil.isNotNull(info) && info.getId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public String checkEmailUnique(SysUserEntity user) {
        Long userId = ObjectUtil.isNull(user.getId()) ? -1L : user.getId();
        SysUserEntity info = baseMapper.checkEmailUnique(user.getEmail());
        if (ObjectUtil.isNotNull(info) && info.getId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    @Override
    public void checkUserAllowed(SysUserEntity user) {
        if (ObjectUtil.isNotNull(user.getId()) && user.isAdmin()) {
            throw new BaseException("不允许操作超级管理员用户");
        }
    }


    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户id
     */
    @Override
    public void checkUserDataScope(Long userId) {
        if (!SecurityUtils.getLoginUser().getUser().isAdmin()) {
            SysUserPageReq sysUserPageReq = new SysUserPageReq();
            sysUserPageReq.setId(userId);
            List<SysUserEntity> users = this.listUsers(sysUserPageReq);
            if (CollUtil.isEmpty(users)) {
                throw new BaseException("没有权限访问用户数据！");
            }
//            if (userId.equals(SecurityUtils.getLoginUser().getUser().getId())) {
//                throw new BaseException("不允许修改自己的角色！");
//            }
        }
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertUser(SysUserEntity user) {
        if (StrUtil.isBlank(user.getDelFlag())) {
            user.setDelFlag(SystemConstants.NORMAL);
        }
        // 新增用户信息
        int rows = baseMapper.insert(user);
        // 新增用户岗位关联
        insertUserPost(user);
        // 新增用户与角色管理
        insertUserRole(user);
        return user.getId();
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateUser(SysUserEntity user) {
        Long userId = user.getId();
        // 删除用户与角色关联
        userRoleMapper.delete(Wrappers.<SysUserRoleEntity>lambdaQuery().eq(SysUserRoleEntity::getUserId, userId));
        // 新增用户与角色管理
        insertUserRole(user);
        // 删除用户与岗位关联
        userPostMapper.delete(Wrappers.<SysUserPostEntity>lambdaQuery().eq(SysUserPostEntity::getUserId, userId));
        // 新增用户与岗位管理
        insertUserPost(user);
        return baseMapper.updateById(user);
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    @Override
    public void insertUserAuth(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(Wrappers.<SysUserRoleEntity>lambdaQuery().eq(SysUserRoleEntity::getUserId, userId));
        insertUserRole(userId, roleIds);
    }

    /**
     * 修改用户状态
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserStatus(SysUserEntity user) {
        // 被改为禁用时
        if (SystemConstants.DEACTIVATE_STATUS.equals(user.getStatus())) {
            SpringContextUtils.getBean(RedisUtils.class).set(SystemConstants.BLACKLIST_KEY + user.getId(), 1, 21L, TimeUnit.DAYS);
        } else {
            SpringContextUtils.getBean(RedisUtils.class).remove(SystemConstants.BLACKLIST_KEY + user.getId());
        }
        return baseMapper.updateById(user);
    }

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserProfile(SysUserEntity user) {
        return baseMapper.updateById(user);
    }

    /**
     * 修改用户头像
     *
     * @param userName 用户名
     * @param avatar   头像地址
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar(String userName, String avatar) {
        return baseMapper.updateUserAvatar(userName, avatar) > 0;
    }

    /**
     * 重置用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int resetPwd(SysUserEntity user) {
        return baseMapper.updateById(user);
    }

    /**
     * 重置用户密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    @Override
    public int resetUserPwd(String userName, String password) {
        return baseMapper.resetUserPwd(userName, password);
    }

    /**
     * 新增用户角色信息
     *
     * @param user 用户对象
     */
    public void insertUserRole(SysUserEntity user) {
        List<Long> roles = user.getRoleIds();
        if (ObjectUtil.isNotNull(roles)) {
            // 新增用户与角色管理
            List<SysUserRoleEntity> list = new ArrayList<SysUserRoleEntity>();
            for (Long roleId : roles) {
                SysUserRoleEntity ur = new SysUserRoleEntity();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                list.add(ur);
            }
            if (!list.isEmpty()) {
                list.forEach(userRoleMapper::insert);
            }
        }
    }

    /**
     * 新增用户岗位信息
     *
     * @param user 用户对象
     */
    public void insertUserPost(SysUserEntity user) {
        List<Long> posts = user.getPostIds();
        if (ObjectUtil.isNotNull(posts)) {
            // 新增用户与岗位管理
            List<SysUserPostEntity> list = new ArrayList<SysUserPostEntity>();
            for (Long postId : posts) {
                SysUserPostEntity up = new SysUserPostEntity();
                up.setUserId(user.getId());
                up.setPostId(postId);
                list.add(up);
            }
            if (!list.isEmpty()) {
                list.forEach(userPostMapper::insert);
            }
        }
    }

    /**
     * 新增用户角色信息
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    public void insertUserRole(Long userId, List<Long> roleIds) {
        if (ObjectUtil.isNotNull(roleIds)) {
            // 新增用户与角色管理
            List<SysUserRoleEntity> list = new ArrayList<SysUserRoleEntity>();
            for (Long roleId : roleIds) {
                SysUserRoleEntity ur = new SysUserRoleEntity();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            if (!list.isEmpty()) {
                list.forEach(userRoleMapper::insert);
            }
        }
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserById(Long userId) {
        // 删除用户与角色关联
        userRoleMapper.delete(Wrappers.<SysUserRoleEntity>lambdaQuery().eq(SysUserRoleEntity::getUserId, userId));
        // 删除用户与岗位表
        userPostMapper.delete(Wrappers.<SysUserPostEntity>lambdaQuery().eq(SysUserPostEntity::getUserId, userId));
        // 拉黑token
        SpringContextUtils.getBean(RedisUtils.class).set(SystemConstants.BLACKLIST_KEY + userId, 1, 21L, TimeUnit.DAYS);
        return baseMapper.deleteById(userId);
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserByIds(List<Long> userIds) {
        for (Long userId : userIds) {
            SysUserEntity sysUserEntity = new SysUserEntity();
            sysUserEntity.setId(userId);
            checkUserAllowed(sysUserEntity);
        }
        // 删除用户与角色关联
        userRoleMapper.delete(Wrappers.<SysUserRoleEntity>lambdaQuery().in(SysUserRoleEntity::getUserId, userIds));
        // 删除用户与岗位关联
        userPostMapper.delete(Wrappers.<SysUserPostEntity>lambdaQuery().in(SysUserPostEntity::getUserId, userIds));
        // 拉黑
        userIds.forEach(userId -> {
            SpringContextUtils.getBean(RedisUtils.class).set(SystemConstants.BLACKLIST_KEY + userId, 1, 21L, TimeUnit.DAYS);
        });
        return baseMapper.deleteBatchIds(userIds);
    }

    /**
     * 插入用户赋值默认数据
     *
     * @param sysUser 用户信息
     * @return 用户Id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long insertUserDefaultData(SysUserEntity sysUser) {
        //默认注册用户角色
        String roleId = configService.getConfigValueByKey(REGISTER_ROLE);
        if (StrUtil.isNotEmpty(roleId) && NumberUtil.isNumber(roleId)) {
            sysUser.setRoleIds(CollUtil.newArrayList(Long.parseLong(roleId)));
        }
        //默认注册部门
        String deptId = configService.getConfigValueByKey(SysConfigConstants.REGISTER_DEPT);
        if (null == sysUser.getDeptId() && StrUtil.isNotEmpty(deptId) && NumberUtil.isNumber(deptId)) {
            sysUser.setDeptId(Long.parseLong(deptId));
        }
        // 默认密码
        String defaultPassword = configService.getConfigValueByKey(SysConfigConstants.DEFAULT_PASSWORD);
        if (StrUtil.isEmpty(sysUser.getPassword())) {
            sysUser.setPassword(SecurityUtils.encryptPassword(StrUtil.emptyToDefault(defaultPassword, "123456")));
        } else {
            sysUser.setPassword(SecurityUtils.encryptPassword(sysUser.getPassword()));
        }
        return this.insertUser(sysUser);
    }

    @Override
    public void assignRoleToUser(Long userId, Long roleId) {
        SysUserRoleEntity ur = new SysUserRoleEntity();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        userRoleMapper.insert(ur);
    }


}
