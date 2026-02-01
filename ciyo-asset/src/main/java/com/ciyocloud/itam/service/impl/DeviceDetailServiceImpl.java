package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.itam.entity.AllocationsEntity;
import com.ciyocloud.itam.entity.DeviceEntity;
import com.ciyocloud.itam.entity.OfferingEntity;
import com.ciyocloud.itam.entity.StocktakeItemsEntity;
import com.ciyocloud.itam.enums.AllocationOwnerType;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.mapper.DeviceMapper;
import com.ciyocloud.itam.service.*;
import com.ciyocloud.itam.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备详情查询 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class DeviceDetailServiceImpl implements DeviceDetailService {

    private final DeviceMapper deviceMapper;
    private final ModelsService modelsService;
    private final OfferingService offeringService;
    private final AllocationsService allocationsService;
    private final AccessoriesService accessoriesService;
    private final LicensesService licensesService;
    private final ConsumablesService consumablesService;
    private final StocktakeItemsService stocktakeItemsService;

    @Override
    public DeviceDetailVO getDeviceDetail(Long id) {
        // 1. 查询设备基本信息
        QueryWrapper<DeviceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("t1.id", id);
        List<DeviceVO> deviceList = deviceMapper.selectListVo(queryWrapper);
        if (deviceList == null || deviceList.isEmpty()) {
            return null;
        }
        DeviceVO deviceVO = deviceList.get(0);
        DeviceDetailVO detailVO = new DeviceDetailVO();
        BeanUtils.copyProperties(deviceVO, detailVO);

        // 2. 查询相关的型号信息
        if (deviceVO.getModelId() != null) {
            QueryWrapper<com.ciyocloud.itam.entity.ModelsEntity> modelWrapper = new QueryWrapper<>();
            modelWrapper.eq("t1.id", deviceVO.getModelId());
            List<ModelsVO> models = modelsService.selectListVo(modelWrapper);
            if (models != null && !models.isEmpty()) {
                detailVO.setModel(models.get(0));
            }
        }

        // 3. 查询分配表，获取关联的资产ID
        // ownerType = ASSET (设备) AND ownerId = id
        QueryWrapper<AllocationsEntity> allocWrapper = new QueryWrapper<>();
        allocWrapper.eq("owner_type", AllocationOwnerType.ASSET).eq("owner_id", id);
        List<AllocationsEntity> allocations = allocationsService.list(allocWrapper);

        // 分类收集ID
        List<Long> accessoryIds = new ArrayList<>();
        List<Long> licenseIds = new ArrayList<>();
        List<Long> consumableIds = new ArrayList<>();
        List<Long> serviceIds = new ArrayList<>();

        if (allocations != null) {
            for (AllocationsEntity alloc : allocations) {
                if (alloc.getItemType() == AssetType.ACCESSORY) {
                    accessoryIds.add(alloc.getItemId());
                } else if (alloc.getItemType() == AssetType.LICENSE) {
                    licenseIds.add(alloc.getItemId());
                } else if (alloc.getItemType() == AssetType.CONSUMABLE) {
                    consumableIds.add(alloc.getItemId());
                } else if (alloc.getItemType() == AssetType.SERVICE) {
                    serviceIds.add(alloc.getItemId());
                }
            }
        }

        // 4. 填充服务信息
        if (!serviceIds.isEmpty()) {
            List<OfferingEntity> list = offeringService.listByIds(serviceIds);
            List<OfferingVO> voList = list.stream().map(e -> {
                OfferingVO vo = new OfferingVO();
                BeanUtils.copyProperties(e, vo);
                return vo;
            }).collect(Collectors.toList());
            detailVO.setServices(voList);
        } else {
            detailVO.setServices(new ArrayList<>());
        }

        // 5. 填充配件信息
        if (!accessoryIds.isEmpty()) {
            QueryWrapper<com.ciyocloud.itam.entity.AccessoriesEntity> accWrapper = new QueryWrapper<>();
            accWrapper.in("t1.id", accessoryIds);
            detailVO.setAccessories(accessoriesService.queryListVo(accWrapper));
        } else {
            detailVO.setAccessories(new ArrayList<>());
        }

        // 6. 填充软件授权信息
        if (!licenseIds.isEmpty()) {
            List<com.ciyocloud.itam.entity.LicensesEntity> list = licensesService.listByIds(licenseIds);
            List<LicensesVO> voList = list.stream().map(e -> {
                LicensesVO vo = new LicensesVO();
                BeanUtils.copyProperties(e, vo);
                return vo;
            }).collect(Collectors.toList());
            detailVO.setLicenses(voList);
        } else {
            detailVO.setLicenses(new ArrayList<>());
        }

        // 7. 填充耗材信息
        if (!consumableIds.isEmpty()) {
            List<com.ciyocloud.itam.entity.ConsumablesEntity> list = consumablesService.listByIds(consumableIds);
            List<ConsumablesVO> voList = list.stream().map(e -> {
                ConsumablesVO vo = new ConsumablesVO();
                BeanUtils.copyProperties(e, vo);
                return vo;
            }).collect(Collectors.toList());
            detailVO.setConsumables(voList);
        } else {
            detailVO.setConsumables(new ArrayList<>());
        }

        return detailVO;
    }

    @Override
    public DeviceScanVO getDeviceDetailByNo(String deviceNo) {
        QueryWrapper<DeviceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_no", deviceNo);
        DeviceEntity deviceEntity = deviceMapper.selectOne(queryWrapper);
        if (deviceEntity == null) {
            return null;
        }

        // 1. 获取基本详情
        DeviceDetailVO detailVO = getDeviceDetail(deviceEntity.getId());
        DeviceScanVO scanVO = new DeviceScanVO();
        BeanUtils.copyProperties(detailVO, scanVO);

        // 2. 查询最近一次盘点信息
        Page<StocktakeItemsEntity> page = new Page<>(1, 1);
        QueryWrapper<StocktakeItemsEntity> stockItemsWrapper = new QueryWrapper<>();
        stockItemsWrapper.eq("asset_id", deviceEntity.getId()).orderByDesc("scanned_at");
        Page<StocktakeItemsEntity> result = stocktakeItemsService.page(page, stockItemsWrapper);
        if (!result.getRecords().isEmpty()) {
            StocktakeItemsEntity stocktakeItem = result.getRecords().get(0);
            scanVO.setLastAuditDate(stocktakeItem.getScannedAt());
            scanVO.setLastAuditStatus(stocktakeItem.getStatus());
            scanVO.setLastAuditNote(stocktakeItem.getNote());
        }

        return scanVO;
    }
}
