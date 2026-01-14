package com.ciyocloud.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.system.entity.SysDeptPostEntity;
import com.ciyocloud.system.entity.SysUserEntity;

import java.util.List;

/**
 * 部门岗位关系Service接口
 *
 * @author codeck-gen
 * @since 2022-06-06 16:25:10
 */
public interface SysDeptPostService extends IService<SysDeptPostEntity> {

    /***
     * 分页查询部门岗位关系
     * @param page 分页对象
     * @param deptPost 分页查询参数
     */
    Page page(Page page, SysDeptPostEntity deptPost);


    /**
     * 根据用户ID获取岗位选择框列表
     *
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    List<Long> getPostListByUserId(Long userId);


    /**
     * 获取用户所在部门的岗位
     *
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    List<Long> getDeptPostListByUserId(Long userId);


    /**
     * 查询部门主管
     *
     * @param deptId 部门ID
     * @return 主管用户Id
     */
    SysUserEntity getDeptLeader(Long deptId);

    /**
     * 查询岗位用户列表
     */
    List<SysUserEntity> getPostUserList(List<Long> postId);
}
