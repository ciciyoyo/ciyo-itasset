package com.ciyocloud.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.system.entity.SysDeptPostEntity;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.vo.SysDeptPostVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门岗位关系Mapper接口
 *
 * @author codeck-gen
 * @since 2022-06-06 16:25:10
 */
public interface SysDeptPostMapper extends BaseMapper<SysDeptPostEntity> {

    /**
     * 分页查询部门岗位关系
     */
    Page<SysDeptPostVO> selectDeptPostPage(@Param("page") Page page, @Param(Constants.WRAPPER) Wrapper<SysDeptPostEntity> queryWrapper);

    /**
     * 查询用户岗位列表
     *
     * @param userId 用户Id
     */
    List<Long> selectPostListByUserId(Long userId);


    /**
     * 获取用户所在部门的岗位
     *
     * @param userId
     * @return
     */
    List<Long> selectDeptPostListByUserId(Long userId);

    /**
     * 查询部门主管
     *
     * @param deptId 部门Id
     */
    List<SysUserEntity> selectDeptLeader(Long deptId);


    /**
     * 查询岗位用户列表
     */
    List<SysUserEntity> selectPostUserList(@Param("postIds") List<Long> postIds);
}
