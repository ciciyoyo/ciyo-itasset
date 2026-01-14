package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.itam.entity.ModelsEntity;
import com.ciyocloud.itam.mapper.ModelsMapper;
import com.ciyocloud.itam.service.ModelsService;
import com.ciyocloud.itam.vo.ModelsVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 型号Service业务层处理
 *
 * @author codeck
 * @since 2026-01-01
 */
@Service
public class ModelsServiceImpl extends ServiceImpl<ModelsMapper, ModelsEntity> implements ModelsService {

    @Override
    public Page<ModelsVO> selectPageVo(IPage<ModelsVO> page, Wrapper<ModelsEntity> queryWrapper) {
        return baseMapper.selectPageVo(page, queryWrapper);
    }

    @Override
    public List<ModelsVO> selectListVo(Wrapper<ModelsEntity> queryWrapper) {
        return baseMapper.selectListVo(queryWrapper);
    }
}
