package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.itam.entity.ConsumableTransactionsEntity;
import com.ciyocloud.itam.entity.ConsumablesEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.enums.ConsumableActionType;
import com.ciyocloud.itam.enums.ConsumableTargetType;
import com.ciyocloud.itam.mapper.ConsumablesMapper;
import com.ciyocloud.itam.service.AssetsMonthlyStatsService;
import com.ciyocloud.itam.service.ConsumableTransactionsService;
import com.ciyocloud.itam.service.ConsumablesService;
import com.ciyocloud.itam.vo.ConsumablesVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 耗材Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Service
@RequiredArgsConstructor
public class ConsumablesServiceImpl extends ServiceImpl<ConsumablesMapper, ConsumablesEntity> implements ConsumablesService {

    private final ConsumableTransactionsService consumableTransactionsService;
    private final AssetsMonthlyStatsService assetsMonthlyStatsService;
    private final com.ciyocloud.itam.util.AssetCodeUtils assetCodeUtils;

    @Override
    public Page<ConsumablesVO> queryPageVo(Page<ConsumablesEntity> page, ConsumablesEntity consumables) {
        QueryWrapper<ConsumablesEntity> queryWrapper = buildQueryWrapper(consumables);
        Page<ConsumablesVO> voPage = new Page<>(page.getCurrent(), page.getSize());
        return baseMapper.selectPageVo(voPage, queryWrapper);
    }

    @Override
    public List<ConsumablesVO> queryListVo(ConsumablesEntity consumables) {
        QueryWrapper<ConsumablesEntity> queryWrapper = buildQueryWrapper(consumables);
        return baseMapper.selectListVo(queryWrapper);
    }

    private QueryWrapper<ConsumablesEntity> buildQueryWrapper(ConsumablesEntity consumables) {
        QueryWrapper<ConsumablesEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(consumables.getName()), "t1.name", consumables.getName());
        queryWrapper.like(StringUtils.isNotBlank(consumables.getItemNo()), "t1.item_no", consumables.getItemNo());
        queryWrapper.like(StringUtils.isNotBlank(consumables.getOrderNumber()), "t1.order_number", consumables.getOrderNumber());
        queryWrapper.eq(consumables.getCategoryId() != null, "t1.category_id", consumables.getCategoryId());
        queryWrapper.eq(consumables.getManufacturerId() != null, "t1.manufacturer_id", consumables.getManufacturerId());
        queryWrapper.eq(consumables.getLocationId() != null, "t1.location_id", consumables.getLocationId());
        queryWrapper.eq("t1.deleted", 0);
        queryWrapper.orderByDesc("t1.create_time");
        return queryWrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean stockIn(ConsumableTransactionsEntity transaction) {
        ConsumablesEntity consumable = getById(transaction.getConsumableId());
        if (consumable == null) {
            throw new RuntimeException("耗材不存在");
        }

        if (transaction.getQuantity() == null || transaction.getQuantity() <= 0) {
            throw new RuntimeException("入库数量必须大于0");
        }

        Long oldQty = consumable.getQuantity() == null ? 0L : consumable.getQuantity();
        Long newQty = oldQty + transaction.getQuantity();
        consumable.setQuantity(newQty);
        updateById(consumable);

        transaction.setActionType(ConsumableActionType.STOCK_IN.getValue());
        transaction.setRemainingQuantity(newQty);
        if (transaction.getTargetType() == null) {
            transaction.setTargetType(ConsumableTargetType.PURCHASE.getValue());
        }

        consumableTransactionsService.save(transaction);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean stockOut(ConsumableTransactionsEntity transaction) {
        ConsumablesEntity consumable = getById(transaction.getConsumableId());
        if (consumable == null) {
            throw new RuntimeException("耗材不存在");
        }

        if (transaction.getQuantity() == null || transaction.getQuantity() <= 0) {
            throw new RuntimeException("领取数量必须大于0");
        }

        Long oldQty = consumable.getQuantity() == null ? 0L : consumable.getQuantity();
        if (oldQty < transaction.getQuantity()) {
            throw new RuntimeException("库存不足，当前库存: " + oldQty);
        }

        Long newQty = oldQty - transaction.getQuantity();
        consumable.setQuantity(newQty);
        updateById(consumable);

        transaction.setActionType(ConsumableActionType.STOCK_OUT.getValue());
        transaction.setRemainingQuantity(newQty);
        if (transaction.getTargetType() == null) {
            transaction.setTargetType(ConsumableTargetType.USER.getValue());
        }

        consumableTransactionsService.save(transaction);
        return true;
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        return baseMapper.selectCountByCategory();
    }

    @Override
    public Map<String, Object> getOverviewStats() {
        // 1. 获取库存统计
        Map<String, Object> stats = baseMapper.selectInventoryStats();
        if (stats == null) {
            stats = new java.util.HashMap<>();
            stats.put("totalQuantity", 0);
            stats.put("totalAmount", 0);
            stats.put("skuCount", 0);
            stats.put("lowStockCount", 0);
        }

        // 2. 获取本月出入库统计
        LocalDateTime startOfMonth = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        Map<String, Object> monthStats = consumableTransactionsService.getMonthStats(startOfMonth);
        if (monthStats != null) {
            stats.putAll(monthStats);
        } else {
            stats.put("monthStockIn", 0);
            stats.put("monthStockOut", 0);
        }

        return stats;
    }


    @Override
    public boolean save(ConsumablesEntity entity) {
        if (StringUtils.isBlank(entity.getItemNo())) {
            // 自动生成
            entity.setItemNo(assetCodeUtils.generate(entity.getCategoryId()));
        } else {
            // 查重
            long count = count(new QueryWrapper<ConsumablesEntity>()
                    .eq("item_no", entity.getItemNo())
                    .ne(entity.getId() != null, "id", entity.getId()));
            if (count > 0) {
                throw new RuntimeException("物品编号已存在");
            }
        }

        boolean result = super.save(entity);
        if (result && entity.getId() != null) {
            // 异步同步统计数据
            assetsMonthlyStatsService.saveOrUpdateStatsByAsset(AssetType.CONSUMABLE, entity.getId());
        }
        return result;
    }

    @Override
    public boolean updateById(ConsumablesEntity entity) {
        if (StringUtils.isNotBlank(entity.getItemNo())) {
            // 查重
            long count = count(new QueryWrapper<ConsumablesEntity>()
                    .eq("item_no", entity.getItemNo())
                    .ne(entity.getId() != null, "id", entity.getId()));
            if (count > 0) {
                throw new RuntimeException("物品编号已存在");
            }
        }

        boolean result = super.updateById(entity);
        if (result && entity.getId() != null) {
            // 重新计算当前月份的统计数据
            assetsMonthlyStatsService.recalculateAssetStats(AssetType.CONSUMABLE, entity.getId(), LocalDate.now());
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            // 删除该资产的所有统计数据
            assetsMonthlyStatsService.deleteStatsByAsset(AssetType.CONSUMABLE, (Long) id);
        }
        return result;
    }

    @Override
    public boolean updateBatchById(Collection<ConsumablesEntity> entityList) {
        boolean result = super.updateBatchById(entityList);
        if (result) {
            // 批量更新时，同步更新统计数据
            for (ConsumablesEntity entity : entityList) {
                if (entity.getId() != null) {
                    assetsMonthlyStatsService.recalculateAssetStats(AssetType.CONSUMABLE, entity.getId(), LocalDate.now());
                }
            }
        }
        return result;
    }

    @Override
    public boolean removeConsumablesByIds(List<Long> ids) {
        boolean result = super.removeByIds(ids);
        if (result) {
            // 批量删除时，同步删除统计数据
            for (Long id : ids) {
                assetsMonthlyStatsService.deleteStatsByAsset(AssetType.CONSUMABLE, id);
            }
        }
        return result;
    }
}
