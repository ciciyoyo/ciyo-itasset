package com.ciyocloud.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.mybatis.service.BaseService;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.request.SysUserPageReq;

import java.util.List;

/**
 * 用户 业务层
 *
 * @author codeck
 */
public interface SysUserService extends BaseService<SysUserEntity> {


    /**
     * 分页查询用户
     *
     * @param page 分页对象
     * @param user 用户信息
     * @return 用户集合
     */
    Page<SysUserEntity> page(Page<SysUserEntity> page, SysUserPageReq user);

    /**
     * 查询用户列表
     *
     * @param user 用户信息
     */
    List<SysUserEntity> listUsers(SysUserPageReq user);


    /**
     * 查询全部有权限的用户id列表
     */
    List<Long> listAuthorizedUserIds();

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息 完整对象 包含部门等
     */
    SysUserEntity getUserByUserName(String userName);

    /**
     * 通过用户名查询用户
     *
     * @param userId 用户名
     * @return 用户对象信息 完整对象 包含部门等
     */
    SysUserEntity getUserByUserId(Long userId);


    /**
     * 根据用户ID查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    String getUserRoleGroup(String userName);

    /**
     * 根据用户ID查询用户所属岗位组
     *
     * @param userName 用户名
     * @return 结果
     */
    String getUserPostGroup(String userName);

    /**
     * 校验用户名称是否唯一
     *
     * @param userName 用户名称
     * @return 结果
     */
    String checkUserNameUnique(String userName);

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    String checkPhoneUnique(SysUserEntity user);

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    String checkEmailUnique(SysUserEntity user);

    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    void checkUserAllowed(SysUserEntity user);

    /**
     * 数据权限校验
     *
     * @param userId
     */
    void checkUserDataScope(Long userId);

    /**
     * 新增用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    Long insertUser(SysUserEntity user);

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    int updateUser(SysUserEntity user);

    /**
     * 用户授权角色
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    void insertUserAuth(Long userId, List<Long> roleIds);

    /**
     * 修改用户状态
     *
     * @param user 用户信息
     * @return 结果
     */
    int updateUserStatus(SysUserEntity user);

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    int updateUserProfile(SysUserEntity user);

    /**
     * 修改用户头像
     *
     * @param userName 用户名
     * @param avatar   头像地址
     * @return 结果
     */
    boolean updateUserAvatar(String userName, String avatar);

    /**
     * 重置用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    int resetPwd(SysUserEntity user);

    /**
     * 重置用户密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    int resetUserPwd(String userName, String password);

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    int deleteUserById(Long userId);

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    int deleteUserByIds(List<Long> userIds);

    /**
     * 插入用户赋值默认数据
     *
     * @param sysUser 用户信息
     * @return 用户Id
     */
    Long insertUserDefaultData(SysUserEntity sysUser);

    /**
     * 给用户分配某个角色的权限
     */
    void assignRoleToUser(Long userId, Long roleId);
}
