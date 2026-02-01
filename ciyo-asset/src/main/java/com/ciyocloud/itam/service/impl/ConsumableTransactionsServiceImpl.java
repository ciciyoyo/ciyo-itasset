package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.mybatis.service.BaseServiceImpl;
import com.ciyocloud.itam.entity.ConsumableTransactionsEntity;
import com.ciyocloud.itam.mapper.ConsumableTransactionsMapper;
import com.ciyocloud.itam.service.ConsumableTransactionsService;
import com.ciyocloud.itam.vo.ConsumableTransactionsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 耗材出入库明细Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Service
@RequiredArgsConstructor
public class ConsumableTransactionsServiceImpl extends BaseServiceImpl<ConsumableTransactionsMapper, ConsumableTransactionsEntity> implements ConsumableTransactionsService {

    @Override
    public Page<ConsumableTransactionsVO> queryPageVo(Page<ConsumableTransactionsEntity> page, ConsumableTransactionsEntity transaction) {
        QueryWrapper<ConsumableTransactionsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(transaction.getConsumableId() != null, "t1.consumable_id", transaction.getConsumableId());
        queryWrapper.eq(transaction.getOperatorId() != null, "t1.operator_id", transaction.getOperatorId());
        queryWrapper.eq(transaction.getTargetId() != null, "t1.target_id", transaction.getTargetId());
        queryWrapper.eq(StringUtils.isNotBlank(transaction.getActionType()), "t1.action_type", transaction.getActionType());
        queryWrapper.eq(StringUtils.isNotBlank(transaction.getTargetType()), "t1.target_type", transaction.getTargetType());
        queryWrapper.orderByDesc("t1.create_time");

        Page<ConsumableTransactionsVO> voPage = new Page<>(page.getCurrent(), page.getSize());
        return baseMapper.selectPageVo(voPage, queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getMonthlyStats() {
        return baseMapper.selectMonthlyStats(LocalDate.now().minusMonths(12));
    }

    @Override
    public Map<String, Object> getMonthStats(java.time.LocalDateTime startTime) {
        return baseMapper.selectMonthStats(startTime);
    }
}
