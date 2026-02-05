package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ciyocloud.common.mybatis.service.BaseServiceImpl;
import com.ciyocloud.itam.entity.*;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.mapper.*;
import com.ciyocloud.itam.service.AssetsMonthlyStatsService;
import com.ciyocloud.itam.util.DepreciationCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 资产月度价值统计 Service 实现类
 *
 * @author codeck
 * @since 2026-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetsMonthlyStatsServiceImpl extends BaseServiceImpl<AssetsMonthlyStatsMapper, AssetsMonthlyStatsEntity> implements AssetsMonthlyStatsService {

    private final DeviceMapper deviceMapper;
    private final AccessoriesMapper accessoriesMapper;
    private final ConsumablesMapper consumablesMapper;
    private final LicensesMapper licensesMapper;
    private final OfferingMapper offeringMapper;
    private final DepreciationsMapper depreciationsMapper;
    private final ModelsMapper modelsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateDailyStats(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        String statsMonth = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 预加载所有折旧规则和型号，避免循环查库
        List<DepreciationsEntity> depreciationsList = depreciationsMapper.selectList(null);
        Map<Long, DepreciationsEntity> depreciationMap = depreciationsList.stream()
                .collect(Collectors.toMap(DepreciationsEntity::getId, Function.identity()));

        List<ModelsEntity> modelsList = modelsMapper.selectList(null);
        Map<Long, ModelsEntity> modelMap = modelsList.stream()
                .collect(Collectors.toMap(ModelsEntity::getId, Function.identity()));

        // 处理所有资产类型
        processDeviceAssets(date, statsMonth, depreciationMap, modelMap);
        processAccessoriesAssets(date, statsMonth, depreciationMap, modelMap);
        processConsumablesAssets(date, statsMonth, depreciationMap, modelMap);
        processLicensesAssets(date, statsMonth, depreciationMap, modelMap);
        processOfferingAssets(date, statsMonth, depreciationMap, modelMap);
    }

    /**
     * 处理设备资产
     */
    private void processDeviceAssets(LocalDate date, String statsMonth,
                                     Map<Long, DepreciationsEntity> depreciationMap,
                                     Map<Long, ModelsEntity> modelMap) {
        List<DeviceEntity> deviceList = deviceMapper.selectList(new LambdaQueryWrapper<DeviceEntity>()
                .eq(DeviceEntity::getDeleted, 0));

        if (CollectionUtils.isEmpty(deviceList)) {
            log.info("No devices found for stats calculation.");
            return;
        }

        for (DeviceEntity device : deviceList) {
            try {
                processDeviceStats(device, date, statsMonth, depreciationMap, modelMap);
            } catch (Exception e) {
                log.error("Error calculating stats for device id: {}", device.getId(), e);
            }
        }
    }

    /**
     * 处理配件资产
     */
    private void processAccessoriesAssets(LocalDate date, String statsMonth,
                                          Map<Long, DepreciationsEntity> depreciationMap,
                                          Map<Long, ModelsEntity> modelMap) {
        List<AccessoriesEntity> accessoriesList = accessoriesMapper.selectList(new LambdaQueryWrapper<AccessoriesEntity>()
                .eq(AccessoriesEntity::getDeleted, 0));

        if (CollectionUtils.isEmpty(accessoriesList)) {
            log.info("No accessories found for stats calculation.");
            return;
        }

        for (AccessoriesEntity accessories : accessoriesList) {
            try {
                processAccessoriesStats(accessories, date, statsMonth, depreciationMap);
            } catch (Exception e) {
                log.error("Error calculating stats for accessories id: {}", accessories.getId(), e);
            }
        }
    }

    /**
     * 处理耗材资产
     */
    private void processConsumablesAssets(LocalDate date, String statsMonth,
                                          Map<Long, DepreciationsEntity> depreciationMap,
                                          Map<Long, ModelsEntity> modelMap) {
        List<ConsumablesEntity> consumablesList = consumablesMapper.selectList(new LambdaQueryWrapper<ConsumablesEntity>()
                .eq(ConsumablesEntity::getDeleted, 0));

        if (CollectionUtils.isEmpty(consumablesList)) {
            log.info("No consumables found for stats calculation.");
            return;
        }

        for (ConsumablesEntity consumable : consumablesList) {
            try {
                processConsumableStats(consumable, date, statsMonth, depreciationMap);
            } catch (Exception e) {
                log.error("Error calculating stats for consumable id: {}", consumable.getId(), e);
            }
        }
    }

    /**
     * 处理许可证资产
     */
    private void processLicensesAssets(LocalDate date, String statsMonth,
                                       Map<Long, DepreciationsEntity> depreciationMap,
                                       Map<Long, ModelsEntity> modelMap) {
        List<LicensesEntity> licensesList = licensesMapper.selectList(new LambdaQueryWrapper<LicensesEntity>()
                .eq(LicensesEntity::getDeleted, 0));

        if (CollectionUtils.isEmpty(licensesList)) {
            log.info("No licenses found for stats calculation.");
            return;
        }

        for (LicensesEntity license : licensesList) {
            try {
                processLicenseStats(license, date, statsMonth, depreciationMap);
            } catch (Exception e) {
                log.error("Error calculating stats for license id: {}", license.getId(), e);
            }
        }
    }

    /**
     * 处理服务资产
     */
    private void processOfferingAssets(LocalDate date, String statsMonth,
                                       Map<Long, DepreciationsEntity> depreciationMap,
                                       Map<Long, ModelsEntity> modelMap) {
        List<OfferingEntity> offeringList = offeringMapper.selectList(null);

        if (CollectionUtils.isEmpty(offeringList)) {
            log.info("No offerings found for stats calculation.");
            return;
        }

        for (OfferingEntity offering : offeringList) {
            try {
                processOfferingStats(offering, date, statsMonth);
            } catch (Exception e) {
                log.error("Error calculating stats for offering id: {}", offering.getId(), e);
            }
        }
    }

    /**
     * 处理设备统计
     */
    private void processDeviceStats(DeviceEntity device, LocalDate date, String statsMonth,
                                    Map<Long, DepreciationsEntity> depreciationMap,
                                    Map<Long, ModelsEntity> modelMap) {
        // 获取型号
        ModelsEntity model = modelMap.get(device.getModelId());
        Long categoryId = (model != null) ? model.getCategoryId() : null;
        Long modelDepreciationId = (model != null) ? model.getDepreciationId() : null;

        // 确定折旧规则 (资产 > 型号)
        Long depreciationId = device.getDepreciationId();
        if (depreciationId == null) {
            depreciationId = modelDepreciationId;
        }

        BigDecimal purchaseCost = device.getPurchaseCost() != null ? device.getPurchaseCost() : BigDecimal.ZERO;
        LocalDate purchaseDate = device.getPurchaseDate();

        // 核心计算
        BigDecimal currentValue = purchaseCost;
        BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

        if (depreciationId != null && purchaseDate != null && depreciationMap.containsKey(depreciationId)) {
            DepreciationsEntity rule = depreciationMap.get(depreciationId);
            currentValue = DepreciationCalculator.calculateCurrentValue(purchaseCost, purchaseDate, date, rule);
            accumulatedDepreciation = DepreciationCalculator.calculateAccumulatedDepreciation(purchaseCost, currentValue);
        }

        // 如果设备已报废，价值由0
        if (com.ciyocloud.itam.enums.DeviceStatus.SCRAPPED.equals(device.getAssetsStatus())) {
            currentValue = BigDecimal.ZERO;
            accumulatedDepreciation = purchaseCost;
        }

        saveOrUpdateStats(AssetType.DEVICE, device.getId(), categoryId, purchaseCost,
                currentValue, accumulatedDepreciation, statsMonth, date);
    }

    /**
     * 处理配件统计
     */
    private void processAccessoriesStats(AccessoriesEntity accessories, LocalDate date, String statsMonth,
                                         Map<Long, DepreciationsEntity> depreciationMap) {
        Long depreciationId = accessories.getDepreciationId();
        BigDecimal purchaseCost = accessories.getPurchaseCost() != null ? accessories.getPurchaseCost() : BigDecimal.ZERO;
        LocalDate purchaseDate = accessories.getPurchaseDate() != null ?
                accessories.getPurchaseDate().toLocalDate() : null;

        // 核心计算
        BigDecimal currentValue = purchaseCost;
        BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

        if (depreciationId != null && purchaseDate != null && depreciationMap.containsKey(depreciationId)) {
            DepreciationsEntity rule = depreciationMap.get(depreciationId);
            currentValue = DepreciationCalculator.calculateCurrentValue(purchaseCost, purchaseDate, date, rule);
            accumulatedDepreciation = DepreciationCalculator.calculateAccumulatedDepreciation(purchaseCost, currentValue);
        }

        saveOrUpdateStats(AssetType.ACCESSORY, accessories.getId(), accessories.getCategoryId(),
                purchaseCost, currentValue, accumulatedDepreciation, statsMonth, date);
    }

    /**
     * 处理耗材统计
     */
    private void processConsumableStats(ConsumablesEntity consumable, LocalDate date, String statsMonth,
                                        Map<Long, DepreciationsEntity> depreciationMap) {
        BigDecimal purchaseCost = consumable.getPurchaseCost() != null ? consumable.getPurchaseCost() : BigDecimal.ZERO;
        LocalDate purchaseDate = consumable.getPurchaseDate() != null ?
                consumable.getPurchaseDate().toLocalDate() : null;

        // 耗材通常不折旧，保持原值
        BigDecimal currentValue = purchaseCost;
        BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

        saveOrUpdateStats(AssetType.CONSUMABLE, consumable.getId(), consumable.getCategoryId(),
                purchaseCost, currentValue, accumulatedDepreciation, statsMonth, date);
    }

    /**
     * 处理许可证统计
     */
    private void processLicenseStats(LicensesEntity license, LocalDate date, String statsMonth,
                                     Map<Long, DepreciationsEntity> depreciationMap) {
        BigDecimal purchaseCost = license.getPurchaseCost() != null ? license.getPurchaseCost() : BigDecimal.ZERO;
        LocalDate purchaseDate = license.getPurchaseDate() != null ?
                license.getPurchaseDate().toLocalDate() : null;

        // 许可证通常不折旧，但可以根据到期日期计算价值
        BigDecimal currentValue = purchaseCost;
        BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

        // 如果许可证已过期，价值为0
        if (license.getExpirationDate() != null && date.isAfter(license.getExpirationDate().toLocalDate())) {
            currentValue = BigDecimal.ZERO;
            accumulatedDepreciation = purchaseCost;
        }

        saveOrUpdateStats(AssetType.LICENSE, license.getId(), license.getCategoryId(),
                purchaseCost, currentValue, accumulatedDepreciation, statsMonth, date);
    }

    /**
     * 处理服务统计
     */
    private void processOfferingStats(OfferingEntity offering, LocalDate date, String statsMonth) {
        BigDecimal cost = offering.getCost() != null ? offering.getCost() : BigDecimal.ZERO;
        LocalDate startDate = offering.getStartDate() != null ? offering.getStartDate().toLocalDate() : null;
        LocalDate endDate = offering.getEndDate() != null ? offering.getEndDate().toLocalDate() : null;

        // 服务的价值计算：如果在服务期内，价值为成本；否则为0
        BigDecimal currentValue = cost;
        BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

        if (endDate != null && date.isAfter(endDate)) {
            currentValue = BigDecimal.ZERO;
            accumulatedDepreciation = cost;
        }

        saveOrUpdateStats(AssetType.SERVICE, offering.getId(), null,
                cost, currentValue, accumulatedDepreciation, statsMonth, date);
    }

    /**
     * 保存或更新统计记录
     */
    private void saveOrUpdateStats(AssetType assetsType, Long assetsId, Long categoryId,
                                   BigDecimal initialValue, BigDecimal currentValue,
                                   BigDecimal accumulatedDepreciation, String statsMonth, LocalDate statsDate) {
        // 查找当月已存在的统计记录
        AssetsMonthlyStatsEntity stats = this.getOne(new LambdaQueryWrapper<AssetsMonthlyStatsEntity>()
                .eq(AssetsMonthlyStatsEntity::getAssetsType, assetsType)
                .eq(AssetsMonthlyStatsEntity::getAssetsId, assetsId)
                .eq(AssetsMonthlyStatsEntity::getStatsMonth, statsMonth));

        if (stats == null) {
            stats = new AssetsMonthlyStatsEntity();
            stats.setCreateTime(LocalDateTime.now());
        }

        stats.setAssetsType(assetsType);
        stats.setAssetsId(assetsId);
        stats.setCategoryId(categoryId);
        stats.setInitialValue(initialValue);
        stats.setCurrentValue(currentValue);
        stats.setAccumulatedDepreciation(accumulatedDepreciation);
        stats.setStatsMonth(statsMonth);
        stats.setStatsDate(statsDate);
        stats.setUpdateTime(java.time.LocalDateTime.now());

        this.saveOrUpdate(stats);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStatsByAsset(AssetType assetsType, Long assetsId) {
        log.info("Deleting stats for asset type: {}, id: {}", assetsType, assetsId);

        LambdaQueryWrapper<AssetsMonthlyStatsEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetsMonthlyStatsEntity::getAssetsType, assetsType)
                .eq(AssetsMonthlyStatsEntity::getAssetsId, assetsId);

        this.remove(wrapper);
        log.info("Successfully deleted stats for asset type: {}, id: {}", assetsType, assetsId);
    }

    @Override
    public void saveOrUpdateStatsByAsset(AssetType assetsType, Long assetsId) {
        log.info("Async save/update stats for asset type: {}, id: {}", assetsType, assetsId);
        // 使用当前日期进行计算
        recalculateAssetStats(assetsType, assetsId, LocalDate.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateAssetStats(AssetType assetsType, Long assetsId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        log.info("Recalculating stats for asset type: {}, id: {}, date: {}", assetsType, assetsId, date);

        String statsMonth = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 预加载折旧规则和型号
        List<DepreciationsEntity> depreciationsList = depreciationsMapper.selectList(null);
        Map<Long, DepreciationsEntity> depreciationMap = depreciationsList.stream()
                .collect(Collectors.toMap(DepreciationsEntity::getId, Function.identity()));

        List<ModelsEntity> modelsList = modelsMapper.selectList(null);
        Map<Long, ModelsEntity> modelMap = modelsList.stream()
                .collect(Collectors.toMap(ModelsEntity::getId, Function.identity()));

        // 根据资产类型处理不同的资产
        switch (assetsType) {
            case DEVICE:
                DeviceEntity device = deviceMapper.selectById(assetsId);
                if (device != null && device.getDeleted() == 0) {
                    processDeviceStats(device, date, statsMonth, depreciationMap, modelMap);
                } else {
                    log.warn("Device not found or deleted: {}", assetsId);
                }
                break;

            case ACCESSORY:
                AccessoriesEntity accessories = accessoriesMapper.selectById(assetsId);
                if (accessories != null && accessories.getDeleted() == 0) {
                    processAccessoriesStats(accessories, date, statsMonth, depreciationMap);
                } else {
                    log.warn("Accessories not found or deleted: {}", assetsId);
                }
                break;

            case CONSUMABLE:
                ConsumablesEntity consumable = consumablesMapper.selectById(assetsId);
                if (consumable != null && consumable.getDeleted() == 0) {
                    processConsumableStats(consumable, date, statsMonth, depreciationMap);
                } else {
                    log.warn("Consumable not found or deleted: {}", assetsId);
                }
                break;

            case LICENSE:
                LicensesEntity license = licensesMapper.selectById(assetsId);
                if (license != null && license.getDeleted() == 0) {
                    processLicenseStats(license, date, statsMonth, depreciationMap);
                } else {
                    log.warn("License not found or deleted: {}", assetsId);
                }
                break;

            case SERVICE:
                OfferingEntity offering = offeringMapper.selectById(assetsId);
                if (offering != null) {
                    processOfferingStats(offering, date, statsMonth);
                } else {
                    log.warn("Offering not found: {}", assetsId);
                }
                break;

            default:
                log.error("Unknown asset type: {}", assetsType);
                throw new IllegalArgumentException("Unknown asset type: " + assetsType);
        }

        log.info("Successfully recalculated stats for asset type: {}, id: {}", assetsType, assetsId);
    }


}
