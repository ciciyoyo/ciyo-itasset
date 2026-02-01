package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.mybatis.service.BaseServiceImpl;
import com.ciyocloud.itam.entity.*;
import com.ciyocloud.itam.enums.AllocationOwnerType;
import com.ciyocloud.itam.enums.AllocationStatus;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.mapper.AllocationsMapper;
import com.ciyocloud.itam.req.AllocationsPageReq;
import com.ciyocloud.itam.service.*;
import com.ciyocloud.itam.vo.AllocationsVO;
import com.ciyocloud.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源分配/领用明细表 服务实现类
 *
 * @author codeck
 * @since 2025-12-30
 */
@Service
public class AllocationsServiceImpl extends BaseServiceImpl<AllocationsMapper, AllocationsEntity> implements AllocationsService {

    @Autowired
    @Lazy
    private DeviceService deviceService;
    @Autowired
    @Lazy
    private LicensesService licensesService;
    @Autowired
    @Lazy
    private AccessoriesService accessoriesService;
    @Autowired
    @Lazy
    private ConsumablesService consumablesService;
    @Autowired
    @Lazy
    private OfferingService offeringService;
    @Autowired
    @Lazy
    private LocationsService locationsService;
    @Autowired
    @Lazy
    private SysUserService userService;

    @Override
    public boolean createAllocation(AssetType itemType, Long itemId, AllocationOwnerType ownerType, Long ownerId, Integer quantity, String note) {
        AllocationsEntity allocation = new AllocationsEntity();
        allocation.setItemType(itemType);
        allocation.setItemId(itemId);
        allocation.setOwnerType(ownerType);
        allocation.setOwnerId(ownerId);
        allocation.setQuantity(quantity != null ? quantity : 1);
        allocation.setStatus(AllocationStatus.ACTIVE);
        allocation.setAssignDate(LocalDateTime.now());
        allocation.setNote(note);
        return this.save(allocation);
    }

    @Override
    public boolean closeAllocation(AssetType itemType, Long itemId) {
        LambdaQueryWrapper<AllocationsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AllocationsEntity::getItemType, itemType)
                .eq(AllocationsEntity::getItemId, itemId)
                .eq(AllocationsEntity::getStatus, AllocationStatus.ACTIVE);
        // 如果有多个，取第一个（通常一个服务/资产只有一个活动分配，软件会有多个但此方法不适用，软件应通过ID解除）
        Page<AllocationsEntity> page = this.page(new Page<>(1, 1,false), queryWrapper);
        if (!page.getRecords().isEmpty()) {
            AllocationsEntity allocation = page.getRecords().get(0);
            allocation.setStatus(AllocationStatus.RETURNED);
            allocation.setReturnDate(LocalDateTime.now());
            return this.updateById(allocation);
        }
        return false;
    }

    @Override
    public boolean closeAllocation(Long id) {
        return deallocate(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean allocate(AllocationsEntity allocation) {
        // 1. 保存分配记录
        if (allocation.getQuantity() == null) {
            allocation.setQuantity(1);
        }
        allocation.setStatus(AllocationStatus.ACTIVE);
        allocation.setAssignDate(LocalDateTime.now());
        boolean saved = this.save(allocation);
        if (!saved) {
            return false;
        }

        // 2. 更新资源状态
        return updateResourceStatus(allocation, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAllocate(AssetType itemType, Long itemId, AllocationOwnerType ownerType, List<Long> ownerIds, String note) {
        if (ownerIds == null || ownerIds.isEmpty()) {
            return false;
        }

        for (Long ownerId : ownerIds) {
            AllocationsEntity allocation = new AllocationsEntity();
            allocation.setItemType(itemType);
            allocation.setItemId(itemId);
            allocation.setOwnerType(ownerType);
            allocation.setOwnerId(ownerId);
            allocation.setQuantity(1);
            allocation.setNote(note);
            if (!this.allocate(allocation)) {
                throw new RuntimeException("分配失败: " + ownerId);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deallocate(Long id) {
        AllocationsEntity allocation = this.getById(id);
        if (allocation == null || !AllocationStatus.ACTIVE.equals(allocation.getStatus())) {
            return false;
        }

        // 1. 更新分配记录
        allocation.setStatus(AllocationStatus.RETURNED);
        allocation.setReturnDate(LocalDateTime.now());
        boolean updated = this.updateById(allocation);
        if (!updated) {
            return false;
        }

        // 2. 还原资源状态
        return updateResourceStatus(allocation, false);
    }

    private boolean updateResourceStatus(AllocationsEntity allocation, boolean isAllocating) {
        Long itemId = allocation.getItemId();
        int quantity = allocation.getQuantity() != null ? allocation.getQuantity() : 1;

        switch (allocation.getItemType()) {
            case DEVICE:
                DeviceEntity asset = deviceService.getById(itemId);
                if (asset != null) {
                    if (isAllocating) {
                        if (AllocationOwnerType.USER.equals(allocation.getOwnerType())) {
                            asset.setAssignedTo(allocation.getOwnerId());
                        } else if (AllocationOwnerType.LOCATION.equals(allocation.getOwnerType())) {
                            asset.setLocationId(allocation.getOwnerId());
                        }
                    } else {
                        asset.setAssignedTo(null);
                    }
                    return deviceService.updateById(asset);
                }
                break;
            case ACCESSORY:
                AccessoriesEntity accessory = accessoriesService.getById(itemId);
                if (accessory != null) {
                    long currentQty = accessory.getQuantity() != null ? accessory.getQuantity() : 0;
                    accessory.setQuantity(isAllocating ? currentQty - quantity : currentQty + quantity);
                    return accessoriesService.updateById(accessory);
                }
                break;
            case CONSUMABLE:
                ConsumablesEntity consumable = consumablesService.getById(itemId);
                if (consumable != null) {
                    long currentQty = consumable.getQuantity() != null ? consumable.getQuantity() : 0;
                    consumable.setQuantity(isAllocating ? currentQty - quantity : currentQty + quantity);
                    return consumablesService.updateById(consumable);
                }
                break;
            case LICENSE:
                LicensesEntity license = licensesService.getById(itemId);
                if (license != null) {
                    long currentSeats = license.getTotalSeats() != null ? license.getTotalSeats() : 0L;
                    license.setTotalSeats(isAllocating ? currentSeats - quantity : currentSeats + quantity);
                    return licensesService.updateById(license);
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public Page<AllocationsVO> queryPageVo(Page<AllocationsVO> page, AllocationsPageReq req) {
        return baseMapper.selectPageVo(page, getQueryWrapper(req));
    }

    @Override
    public List<AllocationsVO> queryListVo(AllocationsPageReq req) {
        return baseMapper.selectListVo(getQueryWrapper(req));
    }

    private QueryWrapper<AllocationsEntity> getQueryWrapper(AllocationsPageReq req) {
        QueryWrapper<AllocationsEntity> queryWrapper = new QueryWrapper<>();
        if (req != null) {
            queryWrapper.eq(req.getItemType() != null, "item_type", req.getItemType())
                    .eq(req.getItemId() != null, "item_id", req.getItemId())
                    .eq(req.getOwnerType() != null, "owner_type", req.getOwnerType())
                    .eq(req.getOwnerId() != null, "owner_id", req.getOwnerId())
                    .eq(req.getStatus() != null, "status", req.getStatus())
                    .like(req.getItemName() != null, "item_name", req.getItemName())
                    .like(req.getOwnerName() != null, "owner_name", req.getOwnerName())
                    .like(req.getNote() != null, "note", req.getNote());
        }
        queryWrapper.orderByDesc("create_time");
        return queryWrapper;
    }
}
