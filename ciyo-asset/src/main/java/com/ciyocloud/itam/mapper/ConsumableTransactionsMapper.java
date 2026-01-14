package com.ciyocloud.itam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.itam.entity.ConsumableTransactionsEntity;
import com.ciyocloud.itam.vo.ConsumableTransactionsVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 耗材出入库明细Mapper接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface ConsumableTransactionsMapper extends BaseMapper<ConsumableTransactionsEntity> {

    /**
     * 查询耗材出入库明细列表
     */
    Page<ConsumableTransactionsVO> selectPageVo(Page<ConsumableTransactionsVO> page, @Param(Constants.WRAPPER) Wrapper<ConsumableTransactionsEntity> queryWrapper);

    /**
     * 统计近12个月出入库数量
     *
     * @param startDate 开始日期
     * @return 统计结果
     */
    @Select("SELECT DATE_FORMAT(t.create_time, '%Y-%m') as month, " +
            "SUM(CASE WHEN t.action_type = 'STOCK_IN' THEN t.quantity ELSE 0 END) as stockIn, " +
            "SUM(CASE WHEN t.action_type = 'STOCK_OUT' THEN t.quantity ELSE 0 END) as stockOut " +
            "FROM itam_consumable_transactions t " +
            "WHERE t.create_time >= #{startDate} " +
            "GROUP BY month " +
            "ORDER BY month")
    List<Map<String, Object>> selectMonthlyStats(@Param("startDate") LocalDate startDate);

    /**
     * 统计指定时间之后的出入库数量
     *
     * @param startTime 开始时间
     * @return 统计结果
     */
    @Select("SELECT " +
            "COALESCE(SUM(CASE WHEN action_type = 'STOCK_IN' THEN quantity ELSE 0 END), 0) as monthStockIn, " +
            "COALESCE(SUM(CASE WHEN action_type = 'STOCK_OUT' THEN quantity ELSE 0 END), 0) as monthStockOut " +
            "FROM itam_consumable_transactions " +
            "WHERE create_time >= #{startTime}")
    Map<String, Object> selectMonthStats(@Param("startTime") java.time.LocalDateTime startTime);
}
