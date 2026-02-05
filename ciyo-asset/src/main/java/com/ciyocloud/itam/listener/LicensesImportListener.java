package com.ciyocloud.itam.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.excel.core.SseProgressExcelListener;
import com.ciyocloud.itam.entity.CategoriesEntity;
import com.ciyocloud.itam.entity.LicensesEntity;
import com.ciyocloud.itam.entity.ManufacturersEntity;
import com.ciyocloud.itam.entity.SuppliersEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.service.*;
import com.ciyocloud.itam.vo.LicensesVO;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 软件授权SSE进度导入监听器
 *
 * @author codeck
 * @since 2026-01-30
 */
@Slf4j
public class LicensesImportListener extends SseProgressExcelListener<LicensesVO> {

    private final LicensesService licensesService;
    private final ManufacturersService manufacturersService;
    private final CategoriesService categoriesService;
    private final SuppliersService suppliersService;
    private final AssetsMonthlyStatsService assetsMonthlyStatsService;
    private final Long operatorId;

    public LicensesImportListener(String progressKey, Long userId) {
        super(progressKey, userId, false, 50, 1000);
        this.licensesService = SpringContextUtils.getBean(LicensesService.class);
        this.manufacturersService = SpringContextUtils.getBean(ManufacturersService.class);
        this.categoriesService = SpringContextUtils.getBean(CategoriesService.class);
        this.suppliersService = SpringContextUtils.getBean(SuppliersService.class);
        this.assetsMonthlyStatsService = SpringContextUtils.getBean(AssetsMonthlyStatsService.class);
        this.operatorId = userId;
    }

    @Override
    public void processData(LicensesVO license, AnalysisContext context) {
        int currentRow = context.readRowHolder().getRowIndex() + 1;

        // 验证必填字段
        if (StrUtil.isBlank(license.getName())) {
            throw new BusinessException(String.format("第%d行 软件名称不能为空", currentRow));
        }
        if (StrUtil.isBlank(license.getLicenseKey())) {
            throw new BusinessException(String.format("第%d行 序列号/密钥不能为空", currentRow));
        }
        if (ObjectUtil.isNull(license.getTotalSeats())) {
            throw new BusinessException(String.format("第%d行 总授权数不能为空", currentRow));
        }
        if (StrUtil.isBlank(license.getManufacturerName())) {
            throw new BusinessException(String.format("第%d行 厂商名称不能为空", currentRow));
        }

        // 根据厂商名称查找厂商ID
        ManufacturersEntity manufacturer = manufacturersService.getOneSafe(
                new LambdaQueryWrapper<ManufacturersEntity>()
                        .eq(ManufacturersEntity::getName, license.getManufacturerName())
        );
        if (manufacturer == null) {
            throw new BusinessException(String.format("第%d行 厂商 '%s' 不存在", currentRow, license.getManufacturerName()));
        }
        license.setManufacturerId(manufacturer.getId());

        // 根据分类名称查找分类ID（可选）
        if (StrUtil.isNotBlank(license.getCategoryName())) {
            CategoriesEntity category = categoriesService.getOneSafe(
                    new LambdaQueryWrapper<CategoriesEntity>()
                            .eq(CategoriesEntity::getName, license.getCategoryName())
                            .eq(CategoriesEntity::getCategoryType, AssetType.LICENSE)
            );
            if (category == null) {
                throw new BusinessException(String.format("第%d行 分类 '%s' 不存在", currentRow, license.getCategoryName()));
            }
            license.setCategoryId(category.getId());
        }

        // 根据供应商名称查找供应商ID（可选）
        if (StrUtil.isNotBlank(license.getSupplierName())) {
            SuppliersEntity supplier = suppliersService.getOneSafe(
                    new LambdaQueryWrapper<SuppliersEntity>()
                            .eq(SuppliersEntity::getName, license.getSupplierName())
            );
            if (supplier == null) {
                throw new BusinessException(String.format("第%d行 供应商 '%s' 不存在", currentRow, license.getSupplierName()));
            }
            license.setSupplierId(supplier.getId());
        }

        // 检查序列号/密钥是否已存在
        LicensesEntity existing = licensesService.getOneSafe(
                new LambdaQueryWrapper<LicensesEntity>()
                        .eq(LicensesEntity::getLicenseKey, license.getLicenseKey())
        );

        if (ObjectUtil.isNull(existing)) {
            // 新增软件授权
            license.setCreateBy(operatorId);
            license.setDeleted(0);
            licensesService.save(license);
        } else {
            // 根据导入数据是否有ID来决定是更新还是跳过
            if (license.getId() != null && license.getId().equals(existing.getId())) {
                // 更新软件授权
                license.setUpdateBy(operatorId);
                // 保留创建信息
                license.setCreateBy(existing.getCreateBy());
                license.setCreateTime(existing.getCreateTime());
                license.setDeleted(existing.getDeleted());
                licensesService.updateById(license);
            } else {
                throw new BusinessException(String.format("第%d行 序列号/密钥 '%s' 已存在", currentRow, license.getLicenseKey()));
            }
        }

        // 如果有价值，同步资产统计数据
        if (license.getPurchaseCost() != null && license.getPurchaseCost().compareTo(BigDecimal.ZERO) > 0) {
            assetsMonthlyStatsService.saveOrUpdateStatsByAsset(AssetType.LICENSE, license.getId());
        }
    }
}
