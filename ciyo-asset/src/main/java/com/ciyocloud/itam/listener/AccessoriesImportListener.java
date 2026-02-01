package com.ciyocloud.itam.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.excel.core.SseProgressExcelListener;
import com.ciyocloud.itam.entity.*;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.service.*;
import com.ciyocloud.itam.util.AssetCodeUtils;
import com.ciyocloud.itam.vo.AccessoriesVO;
import lombok.extern.slf4j.Slf4j;

/**
 * 配件SSE进度导入监听器
 *
 * @author codeck
 * @since 2026-01-30
 */
@Slf4j
public class AccessoriesImportListener extends SseProgressExcelListener<AccessoriesVO> {

    private final AccessoriesService accessoriesService;
    private final CategoriesService categoriesService;
    private final SuppliersService suppliersService;
    private final ManufacturersService manufacturerService;
    private final LocationsService locationService;
    private final DepreciationsService depreciationService;
    private final AssetCodeUtils assetCodeUtils;
    private final AssetsMonthlyStatsService assetsMonthlyStatsService;
    private final Long operatorId;

    public AccessoriesImportListener(String progressKey, Long userId) {
        super(progressKey, userId, false, 50, 1000);
        this.accessoriesService = SpringContextUtils.getBean(AccessoriesService.class);
        this.categoriesService = SpringContextUtils.getBean(CategoriesService.class);
        this.suppliersService = SpringContextUtils.getBean(SuppliersService.class);
        this.manufacturerService = SpringContextUtils.getBean(ManufacturersService.class);
        this.locationService = SpringContextUtils.getBean(LocationsService.class);
        this.depreciationService = SpringContextUtils.getBean(DepreciationsService.class);
        this.assetCodeUtils = SpringContextUtils.getBean(AssetCodeUtils.class);
        this.assetsMonthlyStatsService = SpringContextUtils.getBean(AssetsMonthlyStatsService.class);
        this.operatorId = userId;
    }

    @Override
    public void processData(AccessoriesVO accessory, AnalysisContext context) {
        int currentRow = context.readRowHolder().getRowIndex() + 1;

        // 验证必填字段
        if (StrUtil.isBlank(accessory.getName())) {
            throw new BusinessException(String.format("第%d行 配件名称不能为空", currentRow));
        }
        if (accessory.getCategoryId() == null && StrUtil.isBlank(accessory.getCategoryName())) {
            throw new BusinessException(String.format("第%d行 分类不能为空", currentRow));
        }
        if (accessory.getQuantity() == null) {
            throw new BusinessException(String.format("第%d行 数量不能为空", currentRow));
        }
        if (accessory.getQuantity() <= 0) {
            throw new BusinessException(String.format("第%d行 数量必须大于0", currentRow));
        }

        // 解析分类名称转ID
        if (StrUtil.isNotBlank(accessory.getCategoryName())) {
            CategoriesEntity category = categoriesService.getOneSafe(
                    new LambdaQueryWrapper<CategoriesEntity>()
                            .eq(CategoriesEntity::getName, accessory.getCategoryName())
                            .eq(CategoriesEntity::getCategoryType, AssetType.ACCESSORY)
            );
            if (category == null) {
                throw new BusinessException(String.format("第%d行 分类名称 '%s' 不存在", currentRow, accessory.getCategoryName()));
            }
            accessory.setCategoryId(category.getId());
        }

        // 解析供应商名称转ID
        if (StrUtil.isNotBlank(accessory.getSupplierName())) {
            SuppliersEntity supplier = suppliersService.getOneSafe(
                    new LambdaQueryWrapper<SuppliersEntity>()
                            .eq(SuppliersEntity::getName, accessory.getSupplierName())
            );
            if (supplier == null) {
                throw new BusinessException(String.format("第%d行 供应商名称 '%s' 不存在", currentRow, accessory.getSupplierName()));
            }
            accessory.setSupplierId(supplier.getId());
        }

        // 解析厂商名称转ID
        if (StrUtil.isNotBlank(accessory.getManufacturerName())) {
            ManufacturersEntity manufacturer = manufacturerService.getOneSafe(
                    new LambdaQueryWrapper<ManufacturersEntity>()
                            .eq(ManufacturersEntity::getName, accessory.getManufacturerName())
            );
            if (manufacturer == null) {
                throw new BusinessException(String.format("第%d行 厂商名称 '%s' 不存在", currentRow, accessory.getManufacturerName()));
            }
            accessory.setManufacturerId(manufacturer.getId());
        }

        // 解析存放位置名称转ID
        if (StrUtil.isNotBlank(accessory.getLocationName())) {
            LocationsEntity location = locationService.getOneSafe(
                    new LambdaQueryWrapper<LocationsEntity>()
                            .eq(LocationsEntity::getName, accessory.getLocationName())
            );
            if (location == null) {
                throw new BusinessException(String.format("第%d行 存放位置名称 '%s' 不存在", currentRow, accessory.getLocationName()));
            }
            accessory.setLocationId(location.getId());
        }

        // 解析折旧规则名称转ID
        if (StrUtil.isNotBlank(accessory.getDepreciationName())) {
            DepreciationsEntity depreciation = depreciationService.getOneSafe(
                    new LambdaQueryWrapper<DepreciationsEntity>()
                            .eq(DepreciationsEntity::getName, accessory.getDepreciationName())
            );
            if (depreciation == null) {
                throw new BusinessException(String.format("第%d行 折旧规则名称 '%s' 不存在", currentRow, accessory.getDepreciationName()));
            }
            accessory.setDepreciationId(depreciation.getId());
        }

        // 如果没有提供资产编号，自动生成
        if (StrUtil.isBlank(accessory.getAssetNumber())) {
            accessory.setAssetNumber(assetCodeUtils.generate(accessory.getCategoryId()));
        }

        // 检查资产编号是否已存在（资产编号全局唯一）
        AccessoriesEntity existingByCode = accessoriesService.getOneSafe(
                new LambdaQueryWrapper<AccessoriesEntity>()
                        .eq(AccessoriesEntity::getAssetNumber, accessory.getAssetNumber())
        );

        if (ObjectUtil.isNotNull(existingByCode)) {
            // 如果导入数据有ID且与现有记录匹配，则更新
            if (accessory.getId() != null && accessory.getId().equals(existingByCode.getId())) {
                // 更新配件
                accessory.setUpdateBy(operatorId);
                // 保留创建信息
                accessory.setCreateBy(existingByCode.getCreateBy());
                accessory.setCreateTime(existingByCode.getCreateTime());
                accessory.setDeleted(existingByCode.getDeleted());
                accessoriesService.updateById(accessory);
            } else {
                throw new BusinessException(String.format("第%d行 资产编号 '%s' 已存在", currentRow, accessory.getAssetNumber()));
            }
        } else {
            // 新增配件
            accessory.setCreateBy(operatorId);
            accessory.setDeleted(0);
            accessoriesService.save(accessory);
        }

        // 如果有价值，同步资产统计数据
        if (accessory.getPurchaseCost() != null && accessory.getPurchaseCost().compareTo(java.math.BigDecimal.ZERO) > 0) {
            assetsMonthlyStatsService.saveOrUpdateStatsByAsset(AssetType.ACCESSORY, accessory.getId());
        }
    }
}
