package com.ciyocloud.common.mybatis.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * 自定义 BaseServiceImpl 实现类
 */
public class BaseServiceImpl<M extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T>, T>
        extends ServiceImpl<M, T> implements BaseService<T> {


    @Override
    public T getOneSafe(QueryWrapper<T> queryWrapper) {
        IPage<T> resultPage = baseMapper.selectPage(new Page<T>(0, 1, false), queryWrapper);
        List<T> records = resultPage.getRecords();
        return records.isEmpty() ? null : records.get(0);
    }

    @Override
    public T getOneSafe(LambdaQueryWrapper<T> lambdaQueryWrapper) {
        IPage<T> resultPage = baseMapper.selectPage(new Page<T>(0, 1, false), lambdaQueryWrapper);
        List<T> records = resultPage.getRecords();
        return records.isEmpty() ? null : records.get(0);
    }
}
