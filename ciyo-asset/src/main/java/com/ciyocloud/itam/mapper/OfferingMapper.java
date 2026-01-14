package com.ciyocloud.itam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.itam.entity.OfferingEntity;
import com.ciyocloud.itam.vo.OfferingVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务Mapper接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface OfferingMapper extends BaseMapper<OfferingEntity> {

    /**
     * 分页查询服务列表 (包含关联信息)
     *
     * @param page    分页对象
     * @param wrapper 查询参数
     * @return 分页结果
     */
    Page<OfferingVO> selectOfferingPage(Page<OfferingVO> page, @Param(Constants.WRAPPER) Wrapper<OfferingEntity> wrapper);

    /**
     * 查询服务列表 (包含关联信息)
     *
     * @param wrapper 查询条件
     * @return 列表结果
     */
    List<OfferingVO> selectOfferingList(@Param(Constants.WRAPPER) Wrapper<OfferingEntity> wrapper);

}
