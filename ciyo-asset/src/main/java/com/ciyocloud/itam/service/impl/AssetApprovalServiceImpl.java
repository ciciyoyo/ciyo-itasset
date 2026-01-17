package com.ciyocloud.itam.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.itam.entity.*;
import com.ciyocloud.itam.enums.AllocationOwnerType;
import com.ciyocloud.itam.enums.AllocationStatus;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.enums.DeviceStatus;
import com.ciyocloud.itam.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 资产申请审批处理服务实现
 *
 * @author codeck
 * @since 2026-01-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetApprovalServiceImpl implements AssetApprovalService {

    private final DeviceService deviceService;
    private final AllocationsService allocationsService;
    private final ConsumablesService consumablesService;
    private final ConsumableTransactionsService consumableTransactionsService;
    private final LicensesService licensesService;
    private final AccessoriesService accessoriesService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleApproval(AssetRequestsEntity request, Long allocatedItemId) {
        if (allocatedItemId == null) {
            // 如果未传入, 尝试使用申请单上的itemId
            allocatedItemId = request.getItemId();
        }

        if (allocatedItemId == null) {
            throw new BusinessException("审批通过必须指定分配的资产/物品");
        }

        AssetType itemType = request.getItemType();
        switch (itemType) {
            case DEVICE:
                handleDeviceAllocation(request, allocatedItemId);
                break;
            case ACCESSORY:
                handleAccessoryAllocation(request, allocatedItemId);
                break;
            case CONSUMABLE:
                handleConsumableAllocation(request, allocatedItemId);
                break;
            case LICENSE:
                handleLicenseAllocation(request, allocatedItemId);
                break;
            default:
                log.warn("暂未支持的资产类型自动分配: {}", itemType);
                // 可以选择抛出异常或者仅记录日志
                break;
        }

        // 分配完成后, 更新申请单上的 itemId
        request.setItemId(allocatedItemId);
    }

    /**
     * 处理设备分配逻辑
     */
    private void handleDeviceAllocation(AssetRequestsEntity request, Long deviceId) {
        DeviceEntity device = deviceService.getById(deviceId);
        if (device == null) {
            throw new BusinessException("指定的设备不存在");
        }

        // 核心校验：检查设备状态是否空闲
        if (device.getAssetsStatus() != DeviceStatus.PENDING) {
            throw new BusinessException("设备当前状态为[" + device.getAssetsStatus().getDesc() + "]，无法分配");
        }

        Integer qty = request.getQuantity();
        if (qty == null || qty <= 0) qty = 1;

        // 设备是一物一码, 一般申请数量为 1. 如果申请 > 1 但只分配一个设备ID, 可能会有歧义
        // 这里暂时假设申请数量即为1Record, 或者分配了1个就认为是该动作归属


        // 1. 创建分配记录
        AllocationsEntity allocation = new AllocationsEntity();
        allocation.setItemType(AssetType.DEVICE);
        allocation.setItemId(deviceId);
        allocation.setOwnerType(AllocationOwnerType.USER);
        allocation.setOwnerId(request.getUserId());
        allocation.setQuantity(1);
        allocation.setStatus(AllocationStatus.ACTIVE);
        allocation.setAssignDate(LocalDateTime.now());
        allocation.setNote("来自资产申请审批: " + request.getRequestNo());

        // 如果申请单有预计归还时间, 也设置到分配记录中
        if (ObjectUtil.isNotNull(request.getExpectedReturnTime())) {
            allocation.setExpectedReturnDate(request.getExpectedReturnTime());
        }

        allocationsService.save(allocation);

        // 2. 更新设备状态和归属人
        device.setAssetsStatus(DeviceStatus.DEPLOYED);
        device.setAssignedTo(request.getUserId());
        deviceService.updateById(device);
    }

    /**
     * 处理配件分配逻辑
     */
    private void handleAccessoryAllocation(AssetRequestsEntity request, Long accessoryId) {
        AccessoriesEntity accessory = accessoriesService.getById(accessoryId);
        if (accessory == null) {
            throw new BusinessException("指定的配件不存在");
        }

        // 检查库存
        int requestQty = request.getQuantity() != null ? request.getQuantity() : 1;
        if (accessory.getQuantity() < requestQty) {
            throw new BusinessException("配件库存不足, 当前库存: " + accessory.getQuantity() + ", 申请数量: " + requestQty);
        }

        // 1. 扣减库存
        long oldQuantity = accessory.getQuantity();
        long newQuantity = oldQuantity - requestQty;

        // 使用 LambdaUpdateWrapper 确保并发安全
        boolean updateSuccess = accessoriesService.update(
                new LambdaUpdateWrapper<AccessoriesEntity>()
                        .set(AccessoriesEntity::getQuantity, newQuantity)
                        .eq(AccessoriesEntity::getId, accessoryId)
                        .eq(AccessoriesEntity::getQuantity, oldQuantity) // 乐观锁
        );

        if (!updateSuccess) {
            throw new BusinessException("库存更新失败，请重试");
        }

        // 2. 创建分配记录
        AllocationsEntity allocation = new AllocationsEntity();
        allocation.setItemType(AssetType.ACCESSORY);
        allocation.setItemId(accessoryId);
        allocation.setOwnerType(AllocationOwnerType.USER);
        allocation.setOwnerId(request.getUserId());
        allocation.setQuantity(requestQty);
        allocation.setStatus(AllocationStatus.ACTIVE);
        allocation.setAssignDate(LocalDateTime.now());
        allocation.setNote("来自资产申请审批: " + request.getRequestNo());

        if (ObjectUtil.isNotNull(request.getExpectedReturnTime())) {
            allocation.setExpectedReturnDate(request.getExpectedReturnTime());
        }

        allocationsService.save(allocation);
    }

    /**
     * 处理耗材领用逻辑
     */
    private void handleConsumableAllocation(AssetRequestsEntity request, Long consumableId) {
        ConsumablesEntity consumable = consumablesService.getById(consumableId);
        if (consumable == null) {
            throw new BusinessException("指定的耗材不存在");
        }

        // 检查库存
        int requestQty = request.getQuantity() != null ? request.getQuantity() : 1;
        if (consumable.getQuantity() < requestQty) {
            throw new BusinessException("耗材库存不足, 当前库存: " + consumable.getQuantity() + ", 申请数量: " + requestQty);
        }

        // 1. 扣减库存
        long oldQuantity = consumable.getQuantity();
        long newQuantity = oldQuantity - requestQty;

        // 使用 LambdaUpdateWrapper 确保并发安全 (CAS思路更佳, 这里简单处理)
        boolean updateSuccess = consumablesService.update(
                new LambdaUpdateWrapper<ConsumablesEntity>()
                        .set(ConsumablesEntity::getQuantity, newQuantity)
                        .eq(ConsumablesEntity::getId, consumableId)
                        .eq(ConsumablesEntity::getQuantity, oldQuantity) // 乐观锁简单实现
        );

        if (!updateSuccess) {
            throw new BusinessException("库存更新失败，请重试");
        }

        // 2. 记录出库明细 (Transaction)
        ConsumableTransactionsEntity transaction = new ConsumableTransactionsEntity();
        transaction.setConsumableId(consumableId);
        transaction.setOperatorId(request.getApproverId());
        transaction.setActionType("OUT"); // 出库
        transaction.setQuantity((long) requestQty);
        transaction.setRemainingQuantity(newQuantity);
        transaction.setTargetType("USER");
        transaction.setTargetId(request.getUserId());
        transaction.setNote("来自资产申请审批: " + request.getRequestNo());
        consumableTransactionsService.save(transaction);

        // 3. 创建分配记录 (可选, 用于统一查询个人名下资产)
         AllocationsEntity allocation = new AllocationsEntity();
        allocation.setItemType(AssetType.CONSUMABLE);
        allocation.setItemId(consumableId);
        allocation.setOwnerType(AllocationOwnerType.USER);
        allocation.setOwnerId(request.getUserId());
        allocation.setQuantity(requestQty);
        allocation.setStatus(AllocationStatus.ACTIVE); // 耗材一般领取即消耗, 但为了列表显示可用 ACTIVE
        allocation.setAssignDate(LocalDateTime.now());
        allocation.setNote("耗材领用: " + request.getRequestNo());
        allocationsService.save(allocation);
    }

    /**
     * 处理软件授权分配逻辑
     */
    private void handleLicenseAllocation(AssetRequestsEntity request, Long licenseId) {
        LicensesEntity license = licensesService.getById(licenseId);
        if (license == null) {
            throw new BusinessException("指定的软件授权不存在");
        }

        // 检查剩余授权数 (Allocations表中该license的ACTIVE数量)
        long allocatedCount = allocationsService.count(new QueryWrapper<AllocationsEntity>()
                .eq("item_id", licenseId)
                .eq("item_type", AssetType.LICENSE)
                .eq("status", AllocationStatus.ACTIVE));

        int requestQty = request.getQuantity() != null ? request.getQuantity() : 1;

        if (license.getTotalSeats() < allocatedCount + requestQty) {
            throw new BusinessException("软件授权数量不足, 总数: " + license.getTotalSeats() + ", 已分配: " + allocatedCount + ", 申请: " + requestQty);
        }

        // 1. 创建分配记录
        AllocationsEntity allocation = new AllocationsEntity();
        allocation.setItemType(AssetType.LICENSE);
        allocation.setItemId(licenseId);
        allocation.setOwnerType(AllocationOwnerType.USER);
        allocation.setOwnerId(request.getUserId());
        allocation.setQuantity(requestQty);
        allocation.setStatus(AllocationStatus.ACTIVE);
        allocation.setAssignDate(LocalDateTime.now());
        if (ObjectUtil.isNotNull(request.getExpectedReturnTime())) {
            allocation.setExpectedReturnDate(request.getExpectedReturnTime());
        }
        allocation.setNote("软件授权分配: " + request.getRequestNo());

        allocationsService.save(allocation);

        // 可选: 更新 License 表的某些状态或统计字段
    }
}
