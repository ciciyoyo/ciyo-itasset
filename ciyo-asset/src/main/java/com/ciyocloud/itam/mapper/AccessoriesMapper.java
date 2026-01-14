package com.ciyocloud.itam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.itam.entity.AccessoriesEntity;
import com.ciyocloud.itam.vo.AccessoriesVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 配件Mapper接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:26
 */
public interface AccessoriesMapper extends BaseMapper<AccessoriesEntity> {

    Page<AccessoriesVO> selectPageVo(@Param("page") Page<AccessoriesVO> page, @Param(Constants.WRAPPER) Wrapper<AccessoriesEntity> queryWrapper);

    /**
     * 查询配件列表
     *
     * @param queryWrapper 查询条件
     * @return 结果列表
     */
    List<AccessoriesVO> selectListVo(@Param(Constants.WRAPPER) Wrapper<AccessoriesEntity> queryWrapper);


    /**
     * 统计配件汇总数据
     *
     * @param now      当前日期
     * @param soonDate 即将到期日期（如30天后）
     * @return 统计结果
     */
    @Select("SELECT " +
            "COALESCE(SUM(quantity), 0) as total, " +
            "COALESCE(SUM(CASE WHEN warranty_expiration_date > #{now} AND warranty_expiration_date <= #{soonDate} THEN quantity ELSE 0 END), 0) as soonToExpire, " +
            "COALESCE(SUM(CASE WHEN warranty_expiration_date <= #{now} THEN quantity ELSE 0 END), 0) as expired, " +
            "COUNT(CASE WHEN quantity <= COALESCE(min_quantity, 0) THEN 1 END) as lowStock " +
            "FROM itam_accessories " +
            "WHERE deleted = 0")
    Map<String, Object> selectSummaryStats(@Param("now") LocalDate now, @Param("soonDate") LocalDate soonDate);
}
