package com.ciyocloud.itam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.itam.entity.StocktakesEntity;
import com.ciyocloud.itam.vo.StocktakesVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 盘点任务Mapper接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface StocktakesMapper extends BaseMapper<StocktakesEntity> {

    /**
     * 分页查询盘点任务VO
     */
    Page<StocktakesVO> selectPageVo(@Param("page") Page<StocktakesVO> page, @Param(Constants.WRAPPER) Wrapper<StocktakesEntity> wrapper);

    /**
     * 列表查询
     */
    List<StocktakesVO> selectListVo(@Param(Constants.WRAPPER) Wrapper<StocktakesEntity> wrapper);
}
