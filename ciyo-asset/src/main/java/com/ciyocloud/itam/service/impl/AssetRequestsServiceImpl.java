package com.ciyocloud.itam.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.itam.entity.*;
import com.ciyocloud.itam.enums.AssetRequestStatus;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.mapper.*;
import com.ciyocloud.itam.req.AssetRequestsApprovalReq;
import com.ciyocloud.itam.req.AssetRequestsPageReq;
import com.ciyocloud.itam.req.AssetRequestsSubmitReq;
import com.ciyocloud.itam.service.AssetApprovalService;
import com.ciyocloud.itam.service.AssetRequestsService;
import com.ciyocloud.itam.vo.AssetRequestsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 资产申请服务实现类
 *
 * @author codeck
 * @since 2026-01-10
 */
@Service
@RequiredArgsConstructor
public class AssetRequestsServiceImpl extends ServiceImpl<AssetRequestsMapper, AssetRequestsEntity> implements AssetRequestsService {

    private final AssetApprovalService assetApprovalService;
    private final DeviceMapper deviceMapper;
    private final AccessoriesMapper accessoriesMapper;
    private final ConsumablesMapper consumablesMapper;
    private final LicensesMapper licensesMapper;

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

        Page<AssetRequestsVO> page = baseMapper.selectPageVo(pageReq.toMpPage(), wrapper);
        this.fillItemNames(page.getRecords());
        return page;
    }

    @Override
    public Page<AssetRequestsVO> queryManagePage(PageRequest pageReq, AssetRequestsPageReq req) {
        QueryWrapper<AssetRequestsEntity> wrapper = new QueryWrapper<>();

        wrapper.eq(ObjectUtil.isNotNull(req.getUserId()), "r.user_id", req.getUserId());
        wrapper.eq(ObjectUtil.isNotNull(req.getItemType()), "r.item_type", req.getItemType());
        wrapper.eq(ObjectUtil.isNotNull(req.getStatus()), "r.status", req.getStatus());

        if (ObjectUtil.isNotEmpty(req.getKeyword())) {
             wrapper.and(w -> w.like("r.request_no", req.getKeyword()).or().like("u_curr.nick_name", req.getKeyword()));
        }

        wrapper.eq("r.deleted", 0);
        wrapper.orderByDesc("r.create_time");

        Page<AssetRequestsVO> page = baseMapper.selectPageVo(pageReq.toMpPage(), wrapper);
        this.fillItemNames(page.getRecords());
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRequest(AssetRequestsSubmitReq req) {
        AssetRequestsEntity entity = new AssetRequestsEntity();
        BeanUtils.copyProperties(req, entity);

        entity.setUserId(SecurityUtils.getUserId());
        entity.setRequestNo("REQ" + DateUtil.format(new Date(), "yyMMdd") + RandomUtil.randomNumbers(4));
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
            assetApprovalService.handleApproval(request, req.getAllocatedItemId());
        }

        this.updateById(request);
    }

    /**
     * 填充资产名称
     */
    private void fillItemNames(List<AssetRequestsVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

        // 1. 收集各类型的 ID
        Set<Long> deviceIds = new HashSet<>();
        Set<Long> accessoryIds = new HashSet<>();
        Set<Long> consumableIds = new HashSet<>();
        Set<Long> licenseIds = new HashSet<>();

        for (AssetRequestsVO vo : list) {
            if (vo.getItemId() != null) {
                if (AssetType.DEVICE.equals(vo.getItemType())) {
                    deviceIds.add(vo.getItemId());
                } else if (AssetType.ACCESSORY.equals(vo.getItemType())) {
                    accessoryIds.add(vo.getItemId());
                } else if (AssetType.CONSUMABLE.equals(vo.getItemType())) {
                    consumableIds.add(vo.getItemId());
                } else if (AssetType.LICENSE.equals(vo.getItemType())) {
                    licenseIds.add(vo.getItemId());
                }
            }
        }

        // 2. 批量查询并建立映射
        Map<Long, String> deviceMap = new HashMap<>();
        if (CollUtil.isNotEmpty(deviceIds)) {
            List<DeviceEntity> devices = deviceMapper.selectBatchIds(deviceIds);
            if (CollUtil.isNotEmpty(devices)) {
                deviceMap = devices.stream().collect(Collectors.toMap(DeviceEntity::getId, DeviceEntity::getName));
            }
        }

        Map<Long, String> accessoryMap = new HashMap<>();
        if (CollUtil.isNotEmpty(accessoryIds)) {
            List<AccessoriesEntity> accessories = accessoriesMapper.selectBatchIds(accessoryIds);
            if (CollUtil.isNotEmpty(accessories)) {
                accessoryMap = accessories.stream().collect(Collectors.toMap(AccessoriesEntity::getId, AccessoriesEntity::getName));
            }
        }

        Map<Long, String> consumableMap = new HashMap<>();
        if (CollUtil.isNotEmpty(consumableIds)) {
            List<ConsumablesEntity> consumables = consumablesMapper.selectBatchIds(consumableIds);
            if (CollUtil.isNotEmpty(consumables)) {
                consumableMap = consumables.stream().collect(Collectors.toMap(ConsumablesEntity::getId, ConsumablesEntity::getName));
            }
        }

        Map<Long, String> licenseMap = new HashMap<>();
        if (CollUtil.isNotEmpty(licenseIds)) {
            List<LicensesEntity> licenses = licensesMapper.selectBatchIds(licenseIds);
            if (CollUtil.isNotEmpty(licenses)) {
                licenseMap = licenses.stream().collect(Collectors.toMap(LicensesEntity::getId, LicensesEntity::getName));
            }
        }

        // 3. 回填名称
        for (AssetRequestsVO vo : list) {
            String itemName = null;
            if (vo.getItemId() != null) {
                if (AssetType.DEVICE.equals(vo.getItemType())) {
                    itemName = deviceMap.get(vo.getItemId());
                } else if (AssetType.ACCESSORY.equals(vo.getItemType())) {
                    itemName = accessoryMap.get(vo.getItemId());
                } else if (AssetType.CONSUMABLE.equals(vo.getItemType())) {
                    itemName = consumableMap.get(vo.getItemId());
                } else if (AssetType.LICENSE.equals(vo.getItemType())) {
                    itemName = licenseMap.get(vo.getItemId());
                }
            }
            // 如果查到了具体资产名称，则设置；否则保留 SQL 可能查出来的 categoryName 或者 null
            if (itemName != null) {
                vo.setItemName(itemName);
            }
        }
    }
}
