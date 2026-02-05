package com.ciyocloud.common.mybatis.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 自定义 BaseService 接口，继承 MyBatis-Plus 提供的 IService
 */
public interface BaseService<T> extends IService<T> {
    /**
     * 查询单条记录 只会返回唯一一条记录
     *
     * @param queryWrapper 查询条件
     * @return 单条记录
     */
    T getOneSafe(QueryWrapper<T> queryWrapper);

    /**
     * 查询单条记录 只会返回唯一一条记录
     *
     * @param lambdaQueryWrapper 查询条件
     * @return 单条记录
     */
    T getOneSafe(LambdaQueryWrapper<T> lambdaQueryWrapper);
}
