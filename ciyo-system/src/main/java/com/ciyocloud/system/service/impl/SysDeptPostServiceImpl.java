package com.ciyocloud.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.system.entity.SysDeptPostEntity;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.mapper.SysDeptPostMapper;
import com.ciyocloud.system.service.SysDeptPostService;
import com.ciyocloud.system.vo.SysDeptPostVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门岗位关系Service业务层处理
 *
 * @author codeck-gen
 * @since 2022-06-06 16:25:10
 */
@Service
public class SysDeptPostServiceImpl extends ServiceImpl<SysDeptPostMapper, SysDeptPostEntity> implements SysDeptPostService {


    @Override
    public Page<SysDeptPostVO> page(Page page, SysDeptPostEntity deptPost) {
        QueryWrapper<SysDeptPostEntity> wrapper = Wrappers.query();
        wrapper.eq(null != deptPost.getDeptId(), "dp.dept_id", deptPost.getDeptId())
                .like(StrUtil.isNotBlank(deptPost.getPostShowName()), "dp.post_show_name", deptPost.getPostShowName());
        return baseMapper.selectDeptPostPage(page, wrapper);
    }

    @Override
    public List<Long> getPostListByUserId(Long userId) {
        return baseMapper.selectPostListByUserId(userId);
    }

    @Override
    public List<Long> getDeptPostListByUserId(Long userId) {
        return baseMapper.selectDeptPostListByUserId(userId);
    }

    @Override
    public SysUserEntity getDeptLeader(Long deptId) {
        // 如果存在多个主管，取第一个
        List<SysUserEntity> userEntityList = baseMapper.selectDeptLeader(deptId);
        return CollUtil.getFirst(userEntityList);
    }

    @Override
    public List<SysUserEntity> getPostUserList(List<Long> postIds) {
        return baseMapper.selectPostUserList(postIds);
    }
}
