package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.itam.entity.ModelsEntity;
import com.ciyocloud.itam.vo.ModelsVO;

import java.util.List;

/**
 * 型号Service接口
 *
 * @author codeck
 * @since 2026-01-01
 */
public interface ModelsService extends IService<ModelsEntity> {

    /**
     * 分页查询型号VO
     */
    Page<ModelsVO> selectPageVo(IPage<ModelsVO> page, Wrapper<ModelsEntity> queryWrapper);

    /**
     * 查询型号VO列表
     */
    List<ModelsVO> selectListVo(Wrapper<ModelsEntity> queryWrapper);
}
