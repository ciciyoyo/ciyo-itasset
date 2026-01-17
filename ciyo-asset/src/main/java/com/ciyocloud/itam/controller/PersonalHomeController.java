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
import com.ciyocloud.itam.enums.*;
import com.ciyocloud.itam.req.AssetRequestsPageReq;
import com.ciyocloud.itam.req.DevicePageReq;
import com.ciyocloud.itam.req.ReturnDeviceReq;
import com.ciyocloud.itam.service.AllocationsService;
import com.ciyocloud.itam.service.AssetRequestsService;
import com.ciyocloud.itam.service.DeviceService;
import com.ciyocloud.itam.vo.AssetRequestsVO;
import com.ciyocloud.itam.vo.DeviceVO;
import com.ciyocloud.itam.vo.PersonalStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 个人首页/工作台 控制器
 * <p>
 * 提供个人工作台相关的统计数据、设备列表、申请列表及归还操作等接口。
 *
 * @author codeck
 * @since 2026-01-12
 */
@RestController
@RequestMapping("/itam/personal")
@RequiredArgsConstructor
public class PersonalHomeController {

    private final DeviceService deviceService;
    private final AssetRequestsService assetRequestsService;
    private final AllocationsService allocationsService;

    /**
     * 获取个人首页统计数据
     * <p>
     * 包含：我的设备数量、待处理申请数量、资产使用天数、即将到期资产数量及相关详情。
     *
     * @return 个人首页统计数据 VO
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

        // 1.1 设备描述 (取前两个设备名称拼接，用于展示)
        if (deviceCount > 0) {
            List<DeviceEntity> devices = deviceService.page(new Page<>(1, 2), Wrappers.<DeviceEntity>lambdaQuery()
                    .eq(DeviceEntity::getAssignedTo, userId)
                    .ne(DeviceEntity::getAssetsStatus, DeviceStatus.SCRAPPED)
                    .orderByDesc(DeviceEntity::getCreateTime)).getRecords();
            String deviceDetail = devices.stream()
                    .map(DeviceEntity::getName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
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

        // 2.1 申请描述 (取最新一条申请的类型和日期)
        if (pendingCount > 0) {
            List<AssetRequestsEntity> reqList = assetRequestsService.page(new Page<>(1, 1), Wrappers.<AssetRequestsEntity>lambdaQuery()
                    .eq(AssetRequestsEntity::getUserId, userId)
                    .eq(AssetRequestsEntity::getStatus, AssetRequestStatus.PENDING)
                    .orderByDesc(AssetRequestsEntity::getCreateTime)).getRecords();
            AssetRequestsEntity lastReq = CollUtil.getFirst(reqList);
            if (lastReq != null) {
                String typeName = getTypeName(lastReq);
                String dateStr = DateUtil.format(lastReq.getCreateTime(), "yyyy/MM/dd");
                stats.setPendingRequestDetail(typeName + " - " + dateStr);
            }
        } else {
            stats.setPendingRequestDetail("暂无待处理申请");
        }

        // 3. 使用天数 (以最早分配时间起算)
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
            stats.setExpiringDetail(expiringCount + " 个资产即将到期");
        } else {
            stats.setExpiringDetail("暂无设备即将到期");
        }

        return Result.success(stats);
    }

    /**
     * 获取我的设备列表
     *
     * @param page 分页参数
     * @param req  查询参数
     * @return 分页结果
     */
    @GetMapping("/devices")
    public Result<PageResultVO<DeviceVO>> getMyDevices(PageRequest page, DevicePageReq req) {
        // 强制查询当前用户
        req.setAssignedTo(SecurityUtils.getUserId());
        return Result.success(new PageResultVO<>(deviceService.queryPageVo(page.toMpPage(), req)));
    }

    /**
     * 获取我的申请列表
     *
     * @param page 分页参数
     * @param req  查询参数
     * @return 分页结果
     */
    @GetMapping("/requests")
    public Result<PageResultVO<AssetRequestsVO>> getMyRequests(PageRequest page, AssetRequestsPageReq req) {
        // 强制查询当前用户
        req.setUserId(SecurityUtils.getUserId());
        return Result.success(new PageResultVO<>(assetRequestsService.queryPage(page, req)));
    }

    /**
     * 提交设备归还申请
     *
     * @param req 归还申请参数
     * @return 提交结果
     */
    @PostMapping("/return")
    public Result<Boolean> submitReturn(@RequestBody ReturnDeviceReq req) {
        DeviceEntity device = deviceService.getById(req.getDeviceId());
        if (device == null) {
            return Result.failed("设备不存在");
        }
        if (!createReturnRequest(device, req)) {
            // 目前如果没有开启独立的归还申请流程，则直接执行归还操作
            if (!device.getAssignedTo().equals(SecurityUtils.getUserId())) {
                return Result.failed("您没有该设备的归还权限");
            }
            return Result.success(allocationsService.closeAllocation(AssetType.DEVICE, req.getDeviceId()));
        }
        return Result.success(true);
    }

    /**
     * 创建归还申请记录 (预留方法)
     *
     * @param device 设备实体
     * @param req    归还请求
     * @return 是否创建了申请流程
     */
    private boolean createReturnRequest(DeviceEntity device, ReturnDeviceReq req) {
        // 如果需要创建 AssetRequest 进行审批，在此处实现
        // 目前返回 false 表示直接归还，不走审批流程
        return false;
    }

    /**
     * 获取资产申请类型的中文名称
     */
    private String getTypeName(AssetRequestsEntity request) {
        if (request == null || request.getItemType() == null) {
            return "资产申请";
        }
        String code = request.getItemType().getCode();
        if ("device".equalsIgnoreCase(code)) {
            return "设备申请";
        } else if ("license".equalsIgnoreCase(code)) {
            return "软件申请";
        } else if ("accessory".equalsIgnoreCase(code)) {
            return "配件申请";
        }
        return "资产申请";
    }
}
