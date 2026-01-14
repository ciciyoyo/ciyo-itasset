package com.ciyocloud.itam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.itam.entity.AllocationsEntity;
import com.ciyocloud.itam.vo.AllocationsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源分配/领用明细表 Mapper 接口
 *
 * @author codeck
 * @since 2025-12-30
 */
@Mapper
public interface AllocationsMapper extends BaseMapper<AllocationsEntity> {

    /**
     * 分页查询资源分配列表（关联查询）
     */
    Page<AllocationsVO> selectPageVo(@Param("page") Page<AllocationsVO> page, @Param(Constants.WRAPPER) Wrapper<AllocationsEntity> queryWrapper);

    /**
     * 查询资源分配列表（关联查询）
     */
    List<AllocationsVO> selectListVo(@Param(Constants.WRAPPER) Wrapper<AllocationsEntity> queryWrapper);
}
