package com.ciyocloud.itam.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.excel.core.SseProgressExcelListener;
import com.ciyocloud.itam.entity.OfferingEntity;
import com.ciyocloud.itam.entity.SuppliersEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.enums.OfferingStatus;
import com.ciyocloud.itam.service.AssetsMonthlyStatsService;
import com.ciyocloud.itam.service.OfferingService;
import com.ciyocloud.itam.service.SuppliersService;
import com.ciyocloud.itam.vo.OfferingVO;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务SSE进度导入监听器
 *
 * @author codeck
 * @since 2026-01-30
 */
@Slf4j
public class OfferingImportListener extends SseProgressExcelListener<OfferingVO> {

    private final OfferingService offeringService;
    private final SuppliersService suppliersService;
    private final AssetsMonthlyStatsService assetsMonthlyStatsService;
    private final Long operatorId;

    public OfferingImportListener(String progressKey, Long userId) {
        super(progressKey, userId, false, 50, 1000);
        this.offeringService = SpringContextUtils.getBean(OfferingService.class);
        this.suppliersService = SpringContextUtils.getBean(SuppliersService.class);
        this.assetsMonthlyStatsService = SpringContextUtils.getBean(AssetsMonthlyStatsService.class);
        this.operatorId = userId;
    }

    @Override
    public void processData(OfferingVO offering, AnalysisContext context) {
        int currentRow = context.readRowHolder().getRowIndex() + 1;

        // 验证必填字段
        if (StrUtil.isBlank(offering.getName())) {
            throw new BusinessException(String.format("第%d行 服务名称不能为空", currentRow));
        }
        if (StrUtil.isBlank(offering.getSupplierName())) {
            throw new BusinessException(String.format("第%d行 供应商名称不能为空", currentRow));
        }

        // 根据供应商名称查找供应商ID
        SuppliersEntity supplier = suppliersService.getOneSafe(
                new LambdaQueryWrapper<SuppliersEntity>()
                        .eq(SuppliersEntity::getName, offering.getSupplierName())
        );
        if (supplier == null) {
            throw new BusinessException(String.format("第%d行 供应商 '%s' 不存在", currentRow, offering.getSupplierName()));
        }
        offering.setSupplierId(supplier.getId());



        // 如果没有设置服务状态，默认为正常
        if (offering.getOfferingStatus() == null) {
            offering.setOfferingStatus(OfferingStatus.NORMAL);
        }

        // 检查是否存在相同服务名称和供应商的记录
        OfferingEntity existing = offeringService.getOneSafe(
                new LambdaQueryWrapper<OfferingEntity>()
                        .eq(OfferingEntity::getName, offering.getName())
                        .eq(OfferingEntity::getSupplierId, offering.getSupplierId())
        );

        // 将 VO 转换为 Entity
        OfferingEntity entity = new OfferingEntity();
        entity.setId(offering.getId());
        entity.setName(offering.getName());
        entity.setSupplierId(offering.getSupplierId());
        entity.setServiceNumber(offering.getServiceNumber());
        entity.setStartDate(offering.getStartDate());
        entity.setEndDate(offering.getEndDate());
        entity.setCost(offering.getCost());
        entity.setNotes(offering.getNotes());

        entity.setOfferingStatus(offering.getOfferingStatus());

        if (ObjectUtil.isNull(existing)) {
            // 新增服务
            entity.setCreateBy(operatorId);
            entity.setDeleted(0);
            offeringService.save(entity);
        } else {
            // 根据导入数据是否有ID来决定是更新还是跳过
            if (offering.getId() != null && offering.getId().equals(existing.getId())) {
                // 更新服务
                entity.setUpdateBy(operatorId);
                // 保留创建信息
                entity.setCreateBy(existing.getCreateBy());
                entity.setCreateTime(existing.getCreateTime());
                entity.setDeleted(existing.getDeleted());
                offeringService.updateById(entity);
            } else {
                throw new BusinessException(String.format("供应商 '%s' 下服务 '%s' 已存在",
                    offering.getSupplierName(), offering.getName()));
            }
        }



        // 如果有价值，同步资产统计数据
        if (entity.getCost() != null && entity.getCost().compareTo(java.math.BigDecimal.ZERO) > 0) {
            assetsMonthlyStatsService.saveOrUpdateStatsByAsset(AssetType.SERVICE, entity.getId());
        }
    }


}
