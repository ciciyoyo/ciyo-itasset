package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.mybatis.service.BaseService;
import com.ciyocloud.itam.entity.ModelsEntity;
import com.ciyocloud.itam.vo.ModelsVO;

import java.io.InputStream;
import java.util.List;

/**
 * 型号Service接口
 *
 * @author codeck
 * @since 2026-01-01
 */
public interface ModelsService extends BaseService<ModelsEntity> {

    /**
     * 分页查询型号VO（使用Wrapper）
     */
    Page<ModelsVO> selectPageVo(IPage<ModelsVO> page, Wrapper<ModelsEntity> queryWrapper);

    /**
     * 分页查询型号VO（使用Entity自动构建查询条件）
     */
    Page<ModelsVO> selectPageVo(IPage<ModelsVO> page, ModelsEntity models);

    /**
     * 查询型号VO列表（使用Wrapper）
     */
    List<ModelsVO> selectListVo(Wrapper<ModelsEntity> queryWrapper);

    /**
     * 查询型号VO列表（使用Entity自动构建查询条件）
     */
    List<ModelsVO> selectListVo(ModelsEntity models);
    
    /**
     * 导入型号数据
     *
     * @param inputStream      文件输入流
     * @param originalFilename 原始文件名
     * @param progressKey      前端传递的进度监听key
     * @param userId           用户 id
     */
    void importData(InputStream inputStream, String originalFilename, String progressKey, Long userId);
}
