package com.ciyocloud.itam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.itam.entity.ConsumablesEntity;
import com.ciyocloud.itam.vo.ConsumablesVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 耗材Mapper接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface ConsumablesMapper extends BaseMapper<ConsumablesEntity> {

    /**
     * 分页查询耗材列表（关联查询）
     *
     * @param page         分页参数
     * @param queryWrapper 查询条件
     * @return 耗材列表
     */
    Page<ConsumablesVO> selectPageVo(@Param("page") Page<ConsumablesVO> page, @Param(Constants.WRAPPER) Wrapper<ConsumablesEntity> queryWrapper);

    /**
     * 查询耗材列表（关联查询）
     *
     * @param queryWrapper 查询条件
     * @return 耗材列表
     */
    List<ConsumablesVO> selectListVo(@Param(Constants.WRAPPER) Wrapper<ConsumablesEntity> queryWrapper);

    /**
     * 统计各分类耗材数量
     *
     * @return 统计结果
     */
    @Select("SELECT c.name as name, COALESCE(SUM(t.quantity), 0) as value " +
            "FROM itam_consumables t " +
            "LEFT JOIN itam_categories c ON t.category_id = c.id " +
            "WHERE t.deleted = 0 " +
            "GROUP BY c.name")
    List<Map<String, Object>> selectCountByCategory();

    /**
     * 统计库存总览
     *
     * @return 统计结果
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) as totalQuantity, " +
            "COALESCE(SUM(quantity * IFNULL(purchase_cost, 0)), 0) as totalAmount, " +
            "COUNT(id) as skuCount, " +
            "COUNT(CASE WHEN quantity < min_quantity THEN 1 END) as lowStockCount " +
            "FROM itam_consumables t " +
            "WHERE t.deleted = 0")
    Map<String, Object> selectInventoryStats();
}
