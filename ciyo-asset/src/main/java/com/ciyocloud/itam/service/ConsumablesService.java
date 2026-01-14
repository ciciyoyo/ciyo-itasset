package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.itam.entity.ConsumableTransactionsEntity;
import com.ciyocloud.itam.entity.ConsumablesEntity;
import com.ciyocloud.itam.vo.ConsumablesVO;

import java.util.List;
import java.util.Map;

/**
 * 耗材Service接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface ConsumablesService extends IService<ConsumablesEntity> {

    /**
     * 查询耗材列表VO
     *
     * @param page        分页对象
     * @param consumables 查询条件
     * @return 耗材VO分页对象
     */
    Page<ConsumablesVO> queryPageVo(Page<ConsumablesEntity> page, ConsumablesEntity consumables);

    /**
     * 查询耗材列表VO
     *
     * @param consumables 查询条件
     * @return 耗材VO列表
     */
    List<ConsumablesVO> queryListVo(ConsumablesEntity consumables);

    /**
     * 耗材入库
     *
     * @param transaction 入库信息
     * @return 结果
     */
    Boolean stockIn(ConsumableTransactionsEntity transaction);

    /**
     * 耗材领取/出库
     *
     * @param transaction 领取信息
     * @return 结果
     */
    Boolean stockOut(ConsumableTransactionsEntity transaction);

    /**
     * 批量删除耗材
     *
     * @param ids ID列表
     * @return 结果
     */
    boolean removeConsumablesByIds(List<Long> ids);

    /**
     * 统计各分类耗材数量
     *
     * @return 统计结果
     */
    List<Map<String, Object>> getCategoryStats();

    /**
     * 获取耗材总览统计
     *
     * @return 统计结果
     */
    Map<String, Object> getOverviewStats();
}
