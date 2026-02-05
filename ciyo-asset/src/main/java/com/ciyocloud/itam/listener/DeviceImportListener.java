package com.ciyocloud.itam.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.excel.core.SseProgressExcelListener;
import com.ciyocloud.itam.entity.*;
import com.ciyocloud.itam.enums.AllocationOwnerType;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.service.*;
import com.ciyocloud.itam.vo.DeviceVO;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.service.SysUserService;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备SSE进度导入监听器
 *
 * @author codeck
 * @since 2026-01-30
 */
@Slf4j
public class DeviceImportListener extends SseProgressExcelListener<DeviceVO> {

    private final DeviceService deviceService;
    private final ModelsService modelsService;
    private final SuppliersService suppliersService;
    private final LocationsService locationsService;
    private final DepreciationsService depreciationsService;
    private final SysUserService sysUserService;
    private final AllocationsService allocationsService;
    private final AssetsMonthlyStatsService assetsMonthlyStatsService;
    private final Long operatorId;

    public DeviceImportListener(String progressKey, Long userId) {
        super(progressKey, userId, false, 50, 1000);
        this.deviceService = SpringContextUtils.getBean(DeviceService.class);
        this.modelsService = SpringContextUtils.getBean(ModelsService.class);
        this.suppliersService = SpringContextUtils.getBean(SuppliersService.class);
        this.locationsService = SpringContextUtils.getBean(LocationsService.class);
        this.depreciationsService = SpringContextUtils.getBean(DepreciationsService.class);
        this.sysUserService = SpringContextUtils.getBean(SysUserService.class);
        this.allocationsService = SpringContextUtils.getBean(AllocationsService.class);
        this.assetsMonthlyStatsService = SpringContextUtils.getBean(AssetsMonthlyStatsService.class);
        this.operatorId = userId;
    }

    @Override
    public void processData(DeviceVO device, AnalysisContext context) {
        int currentRow = context.readRowHolder().getRowIndex() + 1;

        // 验证必填字段
        if (StrUtil.isBlank(device.getModelName())) {
            throw new BusinessException(String.format("第%d行 型号名称不能为空", currentRow));
        }
        if (ObjectUtil.isNull(device.getAssetsStatus())) {
            throw new BusinessException(String.format("第%d行 设备状态不能为空", currentRow));
        }

        // 根据型号名称查找型号ID
        ModelsEntity model = modelsService.getOneSafe(new LambdaQueryWrapper<ModelsEntity>().eq(ModelsEntity::getName, device.getModelName()));
        if (model == null) {
            throw new BusinessException(String.format("第%d行 型号 '%s' 不存在", currentRow, device.getModelName()));
        }
        device.setModelId(model.getId());
        device.setCategoryId(model.getCategoryId());

        // 根据供应商名称查找供应商ID（可选）
        if (StrUtil.isNotBlank(device.getSupplierName())) {
            SuppliersEntity supplier = suppliersService.getOneSafe(new LambdaQueryWrapper<SuppliersEntity>().eq(SuppliersEntity::getName, device.getSupplierName()));
            if (supplier == null) {
                throw new BusinessException(String.format("第%d行 供应商 '%s' 不存在", currentRow, device.getSupplierName()));
            }
            device.setSupplierId(supplier.getId());
        }

        // 根据存放位置名称查找位置ID（可选）
        if (StrUtil.isNotBlank(device.getLocationName())) {
            LocationsEntity location = locationsService.getOneSafe(new LambdaQueryWrapper<LocationsEntity>().eq(LocationsEntity::getName, device.getLocationName()));
            if (location == null) {
                throw new BusinessException(String.format("第%d行 存放位置 '%s' 不存在", currentRow, device.getLocationName()));
            }
            device.setLocationId(location.getId());
        }

        // 根据折旧规则名称查找折旧ID（可选）
        if (StrUtil.isNotBlank(device.getDepreciationName())) {
            DepreciationsEntity depreciation = depreciationsService.getOneSafe(new LambdaQueryWrapper<DepreciationsEntity>().eq(DepreciationsEntity::getName, device.getDepreciationName()));
            if (depreciation == null) {
                throw new BusinessException(String.format("第%d行 折旧规则 '%s' 不存在", currentRow, device.getDepreciationName()));
            }
            device.setDepreciationId(depreciation.getId());
        }

        // 根据分配给谁名称查找用户ID（可选）
        if (StrUtil.isNotBlank(device.getAssignedToName())) {
            SysUserEntity user = sysUserService.getOne(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getUserName, device.getAssignedToName()));
            if (user == null) {
                throw new BusinessException(String.format("第%d行 分配给谁 '%s' 不存在", currentRow, device.getAssignedToName()));
            }
            device.setAssignedTo(user.getId());
        }

        // 检查设备是否已存在（按序列号或设备编号）
        DeviceEntity existing = null;
        if (StrUtil.isNotBlank(device.getDeviceNo())) {
            existing = deviceService.getOneSafe(new LambdaQueryWrapper<DeviceEntity>().eq(DeviceEntity::getDeviceNo, device.getDeviceNo()));
        }

        if (ObjectUtil.isNull(existing)) {
            // 新增设备
            device.setCreateBy(operatorId);
            device.setDeleted(0);
            deviceService.save(device);
        } else {
            // 根据导入数据是否有ID来决定是更新还是跳过
            if (device.getId() != null && device.getId().equals(existing.getId())) {
                // 更新设备
                device.setUpdateBy(operatorId);
                // 保留创建信息
                device.setCreateBy(existing.getCreateBy());
                device.setCreateTime(existing.getCreateTime());
                device.setDeleted(existing.getDeleted());
                deviceService.updateById(device);
            } else {
                String identifier = StrUtil.isNotBlank(device.getSerial()) ? "序列号 '" + device.getSerial() + "'" : "设备编号 '" + device.getDeviceNo() + "'";
                throw new BusinessException(String.format("第%d行 %s 已存在", currentRow, identifier));
            }
        }

        // 如果有分配给用户，创建分配关联
        if (device.getAssignedTo() != null) {
            allocationsService.createAllocation(
                    AssetType.DEVICE,
                    device.getId(),
                    AllocationOwnerType.USER,
                    device.getAssignedTo(),
                    1,
                    "导入时分配"
            );
        }

        // 如果有价值，同步资产统计数据
        if (device.getPurchaseCost() != null && device.getPurchaseCost().compareTo(java.math.BigDecimal.ZERO) > 0) {
            assetsMonthlyStatsService.saveOrUpdateStatsByAsset(AssetType.DEVICE, device.getId());
        }
    }
}
