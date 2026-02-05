package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.mybatis.service.BaseService;
import com.ciyocloud.itam.entity.ConsumableTransactionsEntity;
import com.ciyocloud.itam.vo.ConsumableTransactionsVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 耗材出入库明细Service接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface ConsumableTransactionsService extends BaseService<ConsumableTransactionsEntity> {

    /**
     * 查询耗材出入库明细列表
     */
    Page<ConsumableTransactionsVO> queryPageVo(Page<ConsumableTransactionsEntity> page, ConsumableTransactionsEntity transaction);

    /**
     * 统计近12个月出入库数量
     *
     * @return 统计结果
     */
    List<Map<String, Object>> getMonthlyStats();

    /**
     * 统计指定时间之后的出入库数量
     *
     * @param startTime 开始时间
     * @return 统计结果
     */
    Map<String, Object> getMonthStats(LocalDateTime startTime);
}
