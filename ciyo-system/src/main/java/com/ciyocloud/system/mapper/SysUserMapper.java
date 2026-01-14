package com.ciyocloud.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.datapermission.annotation.DataColumn;
import com.ciyocloud.datapermission.annotation.DataPermission;
import com.ciyocloud.system.entity.SysUserEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表 数据层
 *
 * @author codeck
 */
public interface SysUserMapper extends BaseMapper<SysUserEntity> {

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUserEntity selectUserByUserName(String userName);

    /**
     * 通过用户名查询用户
     *
     * @param id id
     * @return 用户对象信息
     */
    SysUserEntity selectUserById(Long id);

    /**
     * 校验用户名称是否唯一
     *
     * @param userName 用户名称
     * @return 结果
     */
    int checkUserNameUnique(String userName);

    /**
     * 校验用户名称是否唯一
     *
     * @param phonenumber 用户名称
     * @return 结果
     */
    SysUserEntity checkPhoneUnique(String phonenumber);

    /**
     * 校验用户名称是否唯一
     *
     * @param email 用户名称
     * @return 结果
     */
    SysUserEntity checkEmailUnique(String email);

    /**
     * 重置用户密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    int resetUserPwd(@Param("userName") String userName, @Param("password") String password);

    /**
     * 修改用户头像
     *
     * @param userName 用户名
     * @param avatar   头像地址
     * @return 结果
     */
    int updateUserAvatar(@Param("userName") String userName, @Param("avatar") String avatar);

    /**
     * 根据条件分页查询用户列表
     *
     * @param queryWrapper 用户信息
     * @return 用户信息集合信息
     */
    @DataPermission({
            @DataColumn(key = "deptName", value = "d.id"),
            @DataColumn(key = "userName", value = "u.id")
    })
    Page<SysUserEntity> selectUserPage(Page<SysUserEntity> page, @Param(Constants.WRAPPER) Wrapper<SysUserEntity> queryWrapper);


    /**
     * 查询用户列表
     *
     * @param queryWrapper 用户信息
     * @return 用户信息集合信息
     */
    @DataPermission({
            @DataColumn(key = "deptName", value = "d.id"),
            @DataColumn(key = "userName", value = "u.id")
    })
    List<SysUserEntity> selectUserList(@Param(Constants.WRAPPER) Wrapper<SysUserEntity> queryWrapper);

    /**
     * 查询所有有权限的用户ID
     *
     * @return 用户ID集合
     */
    @DataPermission({
            @DataColumn(key = "deptName", value = "d.id"),
            @DataColumn(key = "userName", value = "u.id")
    })
    List<Long> selectAuthorizedUserIds();
}
