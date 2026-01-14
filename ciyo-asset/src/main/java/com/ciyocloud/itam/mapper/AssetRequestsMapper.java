package com.ciyocloud.itam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.itam.entity.AssetRequestsEntity;
import com.ciyocloud.itam.vo.AssetRequestsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 资产申请 Mapper 接口
 *
 * @author codeck
 * @since 2026-01-10
 */
@Mapper
public interface AssetRequestsMapper extends BaseMapper<AssetRequestsEntity> {

    Page<AssetRequestsVO> selectPageVo(@Param("page") IPage<AssetRequestsEntity> page, @Param(Constants.WRAPPER) Wrapper<AssetRequestsEntity> wrapper);
}
