package com.ciyocloud.itam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.itam.entity.FailuresEntity;
import com.ciyocloud.itam.vo.FailuresVO;
import com.ciyocloud.itam.vo.SupplierFailureStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 故障表Mapper接口
 *
 * @author codeck
 * @since 2026-01-01
 */
@Mapper
public interface FailuresMapper extends BaseMapper<FailuresEntity> {
    /**
     * 分页查询故障列表
     *
     * @param page    分页对象
     * @param wrapper 查询条件
     * @return 分页结果
     */
    Page<FailuresVO> selectPageVo(Page<FailuresVO> page, @Param(Constants.WRAPPER) Wrapper<FailuresEntity> wrapper);

    /**
     * 统计不同服务商提供的服务出现异常的数量
     *
     * @return 统计列表
     */
    List<SupplierFailureStatsVO> countSupplierFailures();
}
