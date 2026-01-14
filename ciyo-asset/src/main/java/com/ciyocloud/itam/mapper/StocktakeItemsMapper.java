package com.ciyocloud.itam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.itam.entity.StocktakeItemsEntity;
import com.ciyocloud.itam.vo.StocktakeItemsVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 盘点明细Mapper接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface StocktakeItemsMapper extends BaseMapper<StocktakeItemsEntity> {


    /**
     * 分页查询
     */
    Page<StocktakeItemsVO> queryPageVo(Page<StocktakeItemsVO> page, @Param("ew") com.baomidou.mybatisplus.core.conditions.Wrapper<StocktakeItemsEntity> wrapper);

    /**
     * 列表查询
     */
    List<StocktakeItemsVO> queryListVo(@Param("ew") com.baomidou.mybatisplus.core.conditions.Wrapper<StocktakeItemsEntity> wrapper);
}
