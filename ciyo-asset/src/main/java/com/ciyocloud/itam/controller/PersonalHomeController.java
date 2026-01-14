package com.ciyocloud.itam.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.itam.entity.AllocationsEntity;
import com.ciyocloud.itam.entity.AssetRequestsEntity;
import com.ciyocloud.itam.entity.DeviceEntity;
import com.ciyocloud.itam.enums.AllocationOwnerType;
import com.ciyocloud.itam.enums.AllocationStatus;
import com.ciyocloud.itam.enums.AssetRequestStatus;
import com.ciyocloud.itam.enums.DeviceStatus;
import com.ciyocloud.itam.req.AssetRequestsPageReq;
import com.ciyocloud.itam.req.DevicePageReq;
import com.ciyocloud.itam.service.AllocationsService;
import com.ciyocloud.itam.service.DeviceService;
import com.ciyocloud.itam.service.IAssetRequestsService;
import com.ciyocloud.itam.vo.AssetRequestsVO;
import com.ciyocloud.itam.vo.DeviceVO;
import com.ciyocloud.itam.vo.PersonalStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 个人首页/工作台 控制器
 *
 * @author codeck
 * @since 2026-01-12
 */
@RestController
@RequestMapping("/itam/personal")
@RequiredArgsConstructor
public class PersonalHomeController {

    private final DeviceService deviceService;
    private final IAssetRequestsService assetRequestsService;
    private final AllocationsService allocationsService;

    /**
     * 获取个人首页统计数据
     */
    @GetMapping("/stats")
    public Result<PersonalStatsVO> getStats() {
        Long userId = SecurityUtils.getUserId();
        PersonalStatsVO stats = new PersonalStatsVO();

        // 1. 我的设备数量
        LambdaQueryWrapper<DeviceEntity> deviceWrapper = Wrappers.lambdaQuery();
        deviceWrapper.eq(DeviceEntity::getAssignedTo, userId);
        deviceWrapper.ne(DeviceEntity::getAssetsStatus, DeviceStatus.SCRAPPED); // 排除已报废
        long deviceCount = deviceService.count(deviceWrapper);
        stats.setDeviceCount(deviceCount);

        // 1.1 设备描述 (取前两个设备名称)
        if (deviceCount > 0) {
            List<DeviceEntity> devices = deviceService.page(new Page<>(1, 2), Wrappers.<DeviceEntity>lambdaQuery()
                    .eq(DeviceEntity::getAssignedTo, userId)
                    .ne(DeviceEntity::getAssetsStatus, DeviceStatus.SCRAPPED)
                    .orderByDesc(DeviceEntity::getCreateTime)).getRecords();
            String deviceDetail = devices.stream().map(DeviceEntity::getName).reduce((a, b) -> a + ", " + b).orElse("");
            stats.setDeviceDetail(deviceDetail);
        } else {
            stats.setDeviceDetail("暂无设备");
        }

        // 2. 待处理申请数量
        LambdaQueryWrapper<AssetRequestsEntity> requestWrapper = Wrappers.lambdaQuery();
        requestWrapper.eq(AssetRequestsEntity::getUserId, userId);
        requestWrapper.eq(AssetRequestsEntity::getStatus, AssetRequestStatus.PENDING);
        long pendingCount = assetRequestsService.count(requestWrapper);
        stats.setPendingRequestCount(pendingCount);

        // 2.1 申请描述 (取最新一条)
        if (pendingCount > 0) {
            List<AssetRequestsEntity> reqList = assetRequestsService.page(new Page<>(1, 1), Wrappers.<AssetRequestsEntity>lambdaQuery()
                    .eq(AssetRequestsEntity::getUserId, userId)
                    .eq(AssetRequestsEntity::getStatus, AssetRequestStatus.PENDING)
                    .orderByDesc(AssetRequestsEntity::getCreateTime)).getRecords();
            AssetRequestsEntity lastReq = CollUtil.getFirst(reqList);
            if (lastReq != null) {
                String typeName = lastReq.getItemType().getDesc(); // Assuming Enum has desc or similar
                // If Enum doesn't have friendly desc readily available, we might use "资产申请" or switch
                if ("device".equalsIgnoreCase(lastReq.getItemType().getCode())) typeName = "设备申请";
                else if ("license".equalsIgnoreCase(lastReq.getItemType().getCode())) typeName = "软件申请";
                else if ("accessory".equalsIgnoreCase(lastReq.getItemType().getCode())) typeName = "配件申请";
                else typeName = "资产申请";

                String dateStr = DateUtil.format(lastReq.getCreateTime(), "yyyy/MM/dd");
                stats.setPendingRequestDetail(typeName + " - " + dateStr);
            }
        } else {
            stats.setPendingRequestDetail("暂无待处理申请");
        }

        // 3. 使用天数 (最早分配时间至今)
        LambdaQueryWrapper<AllocationsEntity> allocationWrapper = Wrappers.lambdaQuery();
        allocationWrapper.eq(AllocationsEntity::getOwnerType, AllocationOwnerType.USER);
        allocationWrapper.eq(AllocationsEntity::getOwnerId, userId);
        allocationWrapper.orderByAsc(AllocationsEntity::getAssignDate);
        List<AllocationsEntity> allocList = allocationsService.page(new Page<>(1, 1), allocationWrapper).getRecords();
        AllocationsEntity firstAllocation = CollUtil.getFirst(allocList);

        if (firstAllocation != null) {
            long days = DateUtil.betweenDay(DateUtil.parse(firstAllocation.getAssignDate().toString()), DateUtil.date(), true);
            stats.setDaysInUse(days);
            stats.setDaysInUseDetail("自 " + DateUtil.format(firstAllocation.getAssignDate(), "yyyy-MM-dd") + " 起算");
        } else {
            stats.setDaysInUse(0L);
            stats.setDaysInUseDetail("暂无使用记录");
        }

        // 4. 即将到期 (30天内到期)
        LambdaQueryWrapper<AllocationsEntity> expiringWrapper = Wrappers.lambdaQuery();
        expiringWrapper.eq(AllocationsEntity::getOwnerType, AllocationOwnerType.USER);
        expiringWrapper.eq(AllocationsEntity::getOwnerId, userId);
        expiringWrapper.eq(AllocationsEntity::getStatus, AllocationStatus.ACTIVE);
        expiringWrapper.isNotNull(AllocationsEntity::getExpectedReturnDate);
        expiringWrapper.le(AllocationsEntity::getExpectedReturnDate, LocalDateTime.now().plusDays(30));
        long expiringCount = allocationsService.count(expiringWrapper);
        stats.setExpiringCount(expiringCount);

        if (expiringCount > 0) {
            // Get first expiring item name?
            // Allocations table doesn't have name, only itemId.
            // Querying name is expensive if we need to join different tables based on itemType.
            // Simplified: just show "x 个资产即将到期" or try to fetch one if it's a device.
            // Let's just say "x 个资产即将到期" for simplicity unless I want to do a complex join/lookup.
            // Or try to fetch the first one if it is a device.
            stats.setExpiringDetail(expiringCount + " 个资产即将到期");
        } else {
            stats.setExpiringDetail("暂无设备即将到期");
        }

        return Result.success(stats);
    }

    /**
     * 我的设备列表
     */
    @GetMapping("/devices")
    public Result<PageResultVO<DeviceVO>> getMyDevices(PageRequest page, DevicePageReq req) {
        // 强制查询当前用户
        req.setAssignedTo(SecurityUtils.getUserId());
        return Result.success(new PageResultVO<>(deviceService.queryPageVo(page.toMpPage(), req)));
    }

    /**
     * 我的申请列表
     */
    @GetMapping("/requests")
    public Result<PageResultVO<AssetRequestsVO>> getMyRequests(PageRequest page, AssetRequestsPageReq req) {
        // 强制查询当前用户
        req.setUserId(SecurityUtils.getUserId());
        return Result.success(new PageResultVO<>(assetRequestsService.queryPage(page, req)));
    }
}
