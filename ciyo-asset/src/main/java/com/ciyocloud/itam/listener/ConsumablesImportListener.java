package com.ciyocloud.itam.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.excel.core.SseProgressExcelListener;
import com.ciyocloud.itam.entity.CategoriesEntity;
import com.ciyocloud.itam.entity.ConsumablesEntity;
import com.ciyocloud.itam.entity.LocationsEntity;
import com.ciyocloud.itam.entity.ManufacturersEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.service.*;
import com.ciyocloud.itam.vo.ConsumablesVO;
import lombok.extern.slf4j.Slf4j;

/**
 * 耗材SSE进度导入监听器
 *
 * @author codeck
 * @since 2026-01-30
 */
@Slf4j
public class ConsumablesImportListener extends SseProgressExcelListener<ConsumablesVO> {

    private final ConsumablesService consumablesService;
    private final CategoriesService categoriesService;
    private final ManufacturersService manufacturersService;
    private final LocationsService locationsService;
    private final AssetsMonthlyStatsService assetsMonthlyStatsService;
    private final Long operatorId;

    public ConsumablesImportListener(String progressKey, Long userId) {
        super(progressKey, userId, false, 50, 1000);
        this.consumablesService = SpringContextUtils.getBean(ConsumablesService.class);
        this.categoriesService = SpringContextUtils.getBean(CategoriesService.class);
        this.manufacturersService = SpringContextUtils.getBean(ManufacturersService.class);
        this.locationsService = SpringContextUtils.getBean(LocationsService.class);
        this.assetsMonthlyStatsService = SpringContextUtils.getBean(AssetsMonthlyStatsService.class);
        this.operatorId = userId;
    }

    @Override
    public void processData(ConsumablesVO consumable, AnalysisContext context) {
        int currentRow = context.readRowHolder().getRowIndex() + 1;

        // 验证必填字段
        if (StrUtil.isBlank(consumable.getName())) {
            throw new BusinessException(String.format("第%d行 耗材名称不能为空", currentRow));
        }
        if (StrUtil.isBlank(consumable.getCategoryName())) {
            throw new BusinessException(String.format("第%d行 分类名称不能为空", currentRow));
        }
        if (consumable.getQuantity() == null) {
            throw new BusinessException(String.format("第%d行 当前库存数量不能为空", currentRow));
        }

        // 根据分类名称查找分类ID
        CategoriesEntity category = categoriesService.getOneSafe(
                new LambdaQueryWrapper<CategoriesEntity>()
                        .eq(CategoriesEntity::getName, consumable.getCategoryName())
                        .eq(CategoriesEntity::getCategoryType, AssetType.CONSUMABLE)
        );
        if (category == null) {
            throw new BusinessException(String.format("第%d行 分类 '%s' 不存在", currentRow, consumable.getCategoryName()));
        }
        consumable.setCategoryId(category.getId());

        // 根据厂商名称查找厂商ID（可选）
        if (StrUtil.isNotBlank(consumable.getManufacturerName())) {
            ManufacturersEntity manufacturer = manufacturersService.getOneSafe(
                    new LambdaQueryWrapper<ManufacturersEntity>()
                            .eq(ManufacturersEntity::getName, consumable.getManufacturerName())
            );
            if (manufacturer == null) {
                throw new BusinessException(String.format("第%d行 厂商 '%s' 不存在", currentRow, consumable.getManufacturerName()));
            }
            consumable.setManufacturerId(manufacturer.getId());
        }

        // 根据位置名称查找位置ID（可选）
        if (StrUtil.isNotBlank(consumable.getLocationName())) {
            LocationsEntity location = locationsService.getOneSafe(
                    new LambdaQueryWrapper<LocationsEntity>()
                            .eq(LocationsEntity::getName, consumable.getLocationName())
            );
            if (location == null) {
                throw new BusinessException(String.format("第%d行 存放位置 '%s' 不存在", currentRow, consumable.getLocationName()));
            }
            consumable.setLocationId(location.getId());
        }

        // 检查耗材是否已存在（按名称+物品编号组合判断）
        LambdaQueryWrapper<ConsumablesEntity> wrapper = new LambdaQueryWrapper<ConsumablesEntity>()
                .eq(ConsumablesEntity::getName, consumable.getName());

        if (StrUtil.isNotBlank(consumable.getItemNo())) {
            wrapper.eq(ConsumablesEntity::getItemNo, consumable.getItemNo());
        } else {
            wrapper.isNull(ConsumablesEntity::getItemNo);
        }

        ConsumablesEntity existing = consumablesService.getOneSafe(wrapper);

        if (ObjectUtil.isNull(existing)) {
            // 新增耗材
            consumable.setCreateBy(operatorId);
            consumable.setDeleted(0);
            consumablesService.save(consumable);
        } else {
            // 根据导入数据是否有ID来决定是更新还是跳过
            if (consumable.getId() != null && consumable.getId().equals(existing.getId())) {
                // 更新耗材
                consumable.setUpdateBy(operatorId);
                // 保留创建信息
                consumable.setCreateBy(existing.getCreateBy());
                consumable.setCreateTime(existing.getCreateTime());
                consumable.setDeleted(existing.getDeleted());
                consumablesService.updateById(consumable);
            } else {
                String itemInfo = StrUtil.isNotBlank(consumable.getItemNo()) ?
                    String.format("(物品编号:%s)", consumable.getItemNo()) : "";
                throw new BusinessException(String.format("耗材 '%s'%s 已存在", consumable.getName(), itemInfo));
            }
        }

        // 如果有价值，同步资产统计数据
        if (consumable.getPurchaseCost() != null && consumable.getPurchaseCost().compareTo(java.math.BigDecimal.ZERO) > 0) {
            assetsMonthlyStatsService.saveOrUpdateStatsByAsset(AssetType.CONSUMABLE, consumable.getId());
        }
    }
}
