package com.ciyocloud.itam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.itam.entity.ModelsEntity;
import com.ciyocloud.itam.vo.ModelsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 型号Mapper接口
 *
 * @author codeck
 * @since 2026-01-01
 */
@Mapper
public interface ModelsMapper extends BaseMapper<ModelsEntity> {

    /**
     * 分页查询型号VO
     */
    Page<ModelsVO> selectPageVo(IPage<ModelsVO> page, @Param(Constants.WRAPPER) Wrapper<ModelsEntity> queryWrapper);

    /**
     * 查询型号VO列表
     */
    List<ModelsVO> selectListVo(@Param(Constants.WRAPPER) Wrapper<ModelsEntity> queryWrapper);
}
