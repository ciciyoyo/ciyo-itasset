package com.ciyocloud.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.system.entity.SysPostEntity;

import java.util.List;

/**
 * 岗位信息 服务层
 *
 * @author codeck
 */
public interface SysPostService extends IService<SysPostEntity> {


    /**
     * 校验岗位名称
     *
     * @param post 岗位信息
     * @return 结果
     */
    String checkPostNameUnique(SysPostEntity post);

    /**
     * 校验岗位编码
     *
     * @param post 岗位信息
     * @return 结果
     */
    String checkPostCodeUnique(SysPostEntity post);

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    Long countUserPostById(Long postId);


    int deletePostByIds(List<Long> postIds);
}
