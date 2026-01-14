package com.ciyocloud.itam.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.itam.entity.AllocationsEntity;
import com.ciyocloud.itam.entity.AssetRequestsEntity;
import com.ciyocloud.itam.entity.DeviceEntity;
import com.ciyocloud.itam.enums.*;
import com.ciyocloud.itam.mapper.AssetRequestsMapper;
import com.ciyocloud.itam.req.AssetRequestsApprovalReq;
import com.ciyocloud.itam.req.AssetRequestsPageReq;
import com.ciyocloud.itam.req.AssetRequestsSubmitReq;
import com.ciyocloud.itam.service.AllocationsService;
import com.ciyocloud.itam.service.DeviceService;
import com.ciyocloud.itam.service.IAssetRequestsService;
import com.ciyocloud.itam.vo.AssetRequestsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 资产申请服务实现类
 *
 * @author codeck
 * @since 2026-01-10
 */
@Service
@RequiredArgsConstructor
public class AssetRequestsServiceImpl extends ServiceImpl<AssetRequestsMapper, AssetRequestsEntity> implements IAssetRequestsService {

    private final AllocationsService allocationsService;
    private final DeviceService deviceService;
    // Potentially other services like ILicensesService, IAccessoriesService depending on needs

    @Override
    public Page<AssetRequestsVO> queryPage(PageRequest pageReq, AssetRequestsPageReq req) {
        req.setUserId(SecurityUtils.getUserId());
        QueryWrapper<AssetRequestsEntity> wrapper = new QueryWrapper<>();

        wrapper.eq(ObjectUtil.isNotNull(req.getUserId()), "r.user_id", req.getUserId());
        wrapper.eq(ObjectUtil.isNotNull(req.getItemType()), "r.item_type", req.getItemType());
        wrapper.eq(ObjectUtil.isNotNull(req.getStatus()), "r.status", req.getStatus());
        wrapper.eq("r.deleted", 0);

        if (ObjectUtil.isNotEmpty(req.getKeyword())) {
            wrapper.and(w -> w.like("r.request_no", req.getKeyword()));
        }

        wrapper.orderByDesc("r.create_time");

        return baseMapper.selectPageVo(pageReq.toMpPage(), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRequest(AssetRequestsSubmitReq req) {
        AssetRequestsEntity entity = new AssetRequestsEntity();
        BeanUtils.copyProperties(req, entity);

        entity.setUserId(SecurityUtils.getUserId());
        entity.setRequestNo("REQ" + IdUtil.getSnowflakeNextIdStr());
        entity.setStatus(AssetRequestStatus.PENDING);

        this.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRequest(AssetRequestsApprovalReq req) {
        AssetRequestsEntity request = this.getById(req.getId());
        if (request == null) {
            throw new BusinessException("申请不存在");
        }
        // 校验状态是否已处理
        if (request.getStatus() != AssetRequestStatus.PENDING) {
            throw new BusinessException("该申请已被处理，无法重复审批");
        }

        // 更新申请单信息
        request.setStatus(req.getStatus());
        request.setApproverId(SecurityUtils.getUserId());
        request.setApprovalTime(LocalDateTime.now());
        request.setApprovalNote(req.getApprovalNote());

        // 如果是审批通过，则执行资产分配逻辑
        if (req.getStatus() == AssetRequestStatus.APPROVED) {
            if (request.getItemType() == AssetType.DEVICE) {
                Long deviceIdToAllocate = req.getAllocatedItemId();
                // 如果审批参数未指定设备，尝试使用申请单中指定的设备
                if (deviceIdToAllocate == null) {
                    deviceIdToAllocate = request.getItemId();
                }

                if (deviceIdToAllocate == null) {
                    throw new BusinessException("审批通过必须指定分配的设备");
                }
                handleDeviceAllocation(request, deviceIdToAllocate);
            } else {
                // TODO: 处理耗材、软件授权等其他类型的自动分配逻辑
                // 目前仅抛出提示或暂时忽略
            }
        }

        this.updateById(request);
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

        allocationsService.save(allocation);

        // 2. 更新设备状态和归属人
        device.setAssetsStatus(DeviceStatus.DEPLOYED);
        device.setAssignedTo(request.getUserId());
        deviceService.updateById(device);
    }
}
