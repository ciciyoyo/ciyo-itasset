package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.itam.entity.AccessoriesEntity;
import com.ciyocloud.itam.entity.AllocationsEntity;
import com.ciyocloud.itam.entity.DeviceEntity;
import com.ciyocloud.itam.entity.OfferingEntity;
import com.ciyocloud.itam.enums.AllocationOwnerType;
import com.ciyocloud.itam.enums.AllocationStatus;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.enums.OfferingStatus;
import com.ciyocloud.itam.mapper.OfferingMapper;
import com.ciyocloud.itam.req.OfferingPageReq;
import com.ciyocloud.itam.service.*;
import com.ciyocloud.itam.vo.OfferingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 服务 Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Service
@RequiredArgsConstructor
public class OfferingServiceImpl extends ServiceImpl<OfferingMapper, OfferingEntity> implements OfferingService {

    private final SuppliersService suppliersService;
    private final DeviceService deviceService;
    private final AccessoriesService accessoriesService;
    private final AllocationsService allocationsService;
    private final AssetsMonthlyStatsService assetsMonthlyStatsService;
    private final AssetsReportService assetsReportService;


    @Override
    public Page<OfferingVO> queryPageVo(Page<OfferingVO> page, OfferingPageReq req) {
        QueryWrapper<OfferingEntity> wrapper = getWrapper(req);
        Page<OfferingVO> voPage = this.baseMapper.selectOfferingPage(page, wrapper);
        fillOfferingVo(voPage.getRecords());
        return voPage;
    }

    @Override
    public List<OfferingVO> queryListVo(OfferingPageReq req) {
        QueryWrapper<OfferingEntity> wrapper = getWrapper(req);
        List<OfferingVO> list = this.baseMapper.selectOfferingList(wrapper);
        fillOfferingVo(list);
        return list;
    }

    private QueryWrapper<OfferingEntity> getWrapper(OfferingPageReq req) {
        QueryWrapper<OfferingEntity> wrapper = new QueryWrapper<>();
        if (req.getQuickSearchType() != null) {
            if (req.getQuickSearchType() == 1) {
                // 分配到设备的服务
                wrapper.eq("t1.target_type", AssetType.DEVICE);
                wrapper.isNotNull("t1.target_id");
            } else if (req.getQuickSearchType() == 2) {
                // 异常的服务
                wrapper.eq("t1.offering_status", OfferingStatus.EXCEPTION);
            }
        }

        wrapper.like(StringUtils.hasText(req.getName()), "t1.name", req.getName());
        wrapper.like(StringUtils.hasText(req.getServiceNumber()), "t1.service_number", req.getServiceNumber());
        wrapper.eq(req.getOfferingStatus() != null, "t1.offering_status", req.getOfferingStatus());
        wrapper.eq(req.getTargetType() != null, "t1.target_type", req.getTargetType());
        wrapper.eq(req.getTargetId() != null, "t1.target_id", req.getTargetId());
        wrapper.eq(req.getSupplierId() != null, "t1.supplier_id", req.getSupplierId());
        wrapper.eq("t1.deleted", 0);
        wrapper.orderByDesc("t1.create_time");
        return wrapper;
    }

    @Override
    public OfferingVO getOfferingDetail(Long id) {
        OfferingEntity entity = this.getById(id);
        if (entity == null) {
            return null;
        }
        OfferingVO vo = new OfferingVO();
        BeanUtils.copyProperties(entity, vo);
        fillOfferingVo(java.util.Collections.singletonList(vo));
        return vo;
    }

    /**
     * 填充 VO 关联信息 (仅填充多态关联目标，供应商已通过SQL关联)
     */
    private void fillOfferingVo(List<OfferingVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        // 填充关联目标名称 (按类型分组查询)
        // 资产
        fillTargetNames(list, AssetType.DEVICE, ids -> {
            return deviceService.listByIds(ids).stream()
                    .collect(Collectors.toMap(DeviceEntity::getId, DeviceEntity::getName));
        });

        // 配件
        fillTargetNames(list, AssetType.ACCESSORY, ids -> {
            return accessoriesService.listByIds(ids).stream()
                    .collect(Collectors.toMap(AccessoriesEntity::getId, AccessoriesEntity::getName));
        });


        // 填充关联时间
        fillAssignDate(list);
    }

    private void fillTargetNames(List<OfferingVO> list,
                                 AssetType type,
                                 Function<Set<Long>, Map<Long, String>> fetchNameMap) {

        Set<Long> targetIds = list.stream()
                .filter(vo -> type.equals(vo.getTargetType()) && vo.getTargetId() != null)
                .map(OfferingEntity::getTargetId)
                .collect(Collectors.toSet());

        if (!CollectionUtils.isEmpty(targetIds)) {
            Map<Long, String> nameMap = fetchNameMap.apply(targetIds);
            list.forEach(vo -> {
                if (type.equals(vo.getTargetType()) && vo.getTargetId() != null) {
                    vo.setTargetName(nameMap.get(vo.getTargetId()));
                }
            });
        }
    }

    private void fillAssignDate(List<OfferingVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<Long> offeringIds = list.stream().map(OfferingVO::getId).collect(Collectors.toList());

        List<AllocationsEntity> allocations = allocationsService.lambdaQuery()
                .eq(AllocationsEntity::getItemType, AssetType.SERVICE)
                .in(AllocationsEntity::getItemId, offeringIds)
                .eq(AllocationsEntity::getStatus, AllocationStatus.ACTIVE)
                .list();

        Map<Long, LocalDateTime> dateMap = allocations.stream()
                .collect(Collectors.toMap(AllocationsEntity::getItemId, AllocationsEntity::getAssignDate, (v1, v2) -> v1));

        list.forEach(vo -> {
            if (dateMap.containsKey(vo.getId())) {
                vo.setAssignDate(dateMap.get(vo.getId()));
            }
        });
    }

    @Override
    public void bindAsset(Long offeringId, Long assetId) {
        // 1. Update Offering
        OfferingEntity entity = new OfferingEntity();
        entity.setId(offeringId);
        entity.setTargetType(AssetType.DEVICE);
        entity.setTargetId(assetId);
        this.updateById(entity);

        // 2. Record Allocation
        allocationsService.createAllocation(
                AssetType.SERVICE,
                offeringId,
                AllocationOwnerType.ASSET,
                assetId,
                1,
                null
        );
    }

    @Override
    public void unbind(Long offeringId) {
        // 1. close allocation
        allocationsService.closeAllocation(AssetType.SERVICE, offeringId);

        // 2. clear offering target
        LambdaUpdateWrapper<OfferingEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OfferingEntity::getId, offeringId)
                .set(OfferingEntity::getTargetType, null)
                .set(OfferingEntity::getTargetId, null);

        this.update(updateWrapper);
    }


    @Override
    public Map<String, Object> getOfferingStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalCount = this.count();
        long normalCount = this.count(new LambdaQueryWrapper<OfferingEntity>()
                .eq(OfferingEntity::getOfferingStatus, OfferingStatus.NORMAL));
        long exceptionCount = this.count(new LambdaQueryWrapper<OfferingEntity>()
                .eq(OfferingEntity::getOfferingStatus, OfferingStatus.EXCEPTION));
        stats.put("totalCount", totalCount);
        stats.put("normalCount", normalCount);
        stats.put("exceptionCount", exceptionCount);
        stats.put("totalAmount", assetsReportService.getLatestAssetValue(AssetType.SERVICE));

        return stats;
    }


    @Override
    public boolean save(OfferingEntity entity) {
        boolean result = super.save(entity);
        if (result && entity.getId() != null) {
            // 异步同步统计数据
            assetsMonthlyStatsService.saveOrUpdateStatsByAsset(AssetType.SERVICE, entity.getId());
        }
        return result;
    }

    @Override
    public boolean updateById(OfferingEntity entity) {
        boolean result = super.updateById(entity);
        if (result && entity.getId() != null) {
            // 重新计算当前月份的统计数据
            assetsMonthlyStatsService.recalculateAssetStats(AssetType.SERVICE, entity.getId(), LocalDate.now());
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            // 删除该资产的所有统计数据
            assetsMonthlyStatsService.deleteStatsByAsset(AssetType.SERVICE, (Long) id);
        }
        return result;
    }


    @Override
    public boolean removeOfferingsByIds(List<Long> ids) {
        boolean result = super.removeByIds(ids);
        if (result) {
            for (Long id : ids) {
                // 删除该资产的所有统计数据
                assetsMonthlyStatsService.deleteStatsByAsset(AssetType.SERVICE, id);
            }
        }
        return result;
    }

}
