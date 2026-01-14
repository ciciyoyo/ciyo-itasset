package com.ciyocloud.system.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.exception.BaseException;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysPostEntity;
import com.ciyocloud.system.entity.SysUserPostEntity;
import com.ciyocloud.system.mapper.SysPostMapper;
import com.ciyocloud.system.mapper.SysUserPostMapper;
import com.ciyocloud.system.service.SysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 岗位信息 服务层处理
 *
 * @author codeck
 */
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPostEntity> implements SysPostService {
    private final SysPostMapper postMapper;

    private final SysUserPostMapper userPostMapper;


    /**
     * 校验岗位名称是否唯一
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public String checkPostNameUnique(SysPostEntity post) {
        Long postId = ObjectUtil.isNull(post.getId()) ? -1L : post.getId();
        SysPostEntity info = postMapper.checkPostNameUnique(post.getPostName());
        if (ObjectUtil.isNotNull(info) && info.getId().longValue() != postId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验岗位编码是否唯一
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public String checkPostCodeUnique(SysPostEntity post) {
        Long postId = ObjectUtil.isNull(post.getId()) ? -1L : post.getId();
        SysPostEntity info = postMapper.checkPostCodeUnique(post.getPostCode());
        if (ObjectUtil.isNotNull(info) && info.getId().longValue() != postId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public Long countUserPostById(Long postId) {
        return userPostMapper.selectCount(Wrappers.<SysUserPostEntity>lambdaQuery().eq(SysUserPostEntity::getPostId, postId));
    }


    /**
     * 批量删除岗位信息
     *
     * @param postIds 需要删除的岗位ID
     * @return 结果
     * @throws Exception 异常
     */
    @Override
    public int deletePostByIds(List<Long> postIds) {
        for (Long postId : postIds) {
            SysPostEntity post = this.getById(postId);
            if (countUserPostById(postId) > 0) {
                throw new BaseException(String.format("%1$s已分配,不能删除", post.getPostName()));
            }
        }
        return postMapper.deleteBatchIds(postIds);
    }


}
