package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.itam.entity.AllocationsEntity;
import com.ciyocloud.itam.entity.LicensesEntity;
import com.ciyocloud.itam.enums.AllocationStatus;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.mapper.LicensesMapper;
import com.ciyocloud.itam.req.LicenseAllocationReq;
import com.ciyocloud.itam.req.LicensePageReq;
import com.ciyocloud.itam.service.AllocationsService;
import com.ciyocloud.itam.service.AssetsMonthlyStatsService;
import com.ciyocloud.itam.service.LicensesService;
import com.ciyocloud.itam.vo.LicenseAllocationVO;
import com.ciyocloud.itam.vo.LicensesVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 软件授权Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@RequiredArgsConstructor
@Service
public class LicensesServiceImpl extends ServiceImpl<LicensesMapper, LicensesEntity> implements LicensesService {

    private final AllocationsService allocationsService;
    private final AssetsMonthlyStatsService assetsMonthlyStatsService;


    @Override
    public Page<LicensesVO> queryPageVo(Page<LicensesEntity> page, LicensePageReq req) {
        Page<LicensesVO> voPage = new Page<>(page.getCurrent(), page.getSize());
        return baseMapper.selectPageVo(voPage, buildQueryWrapper(req));
    }

    private QueryWrapper<LicensesEntity> buildQueryWrapper(LicensePageReq req) {
        QueryWrapper<LicensesEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("t1.deleted", 0);
        if (req == null) {
            return queryWrapper;
        }
        queryWrapper.like(StringUtils.isNotBlank(req.getName()), "t1.name", req.getName());
        queryWrapper.like(StringUtils.isNotBlank(req.getLicenseKey()), "t1.license_key", req.getLicenseKey());
        queryWrapper.eq(req.getManufacturerId() != null, "t1.manufacturer_id", req.getManufacturerId());
        queryWrapper.eq(req.getCategoryId() != null, "t1.category_id", req.getCategoryId());
        queryWrapper.eq(req.getSupplierId() != null, "t1.supplier_id", req.getSupplierId());
        queryWrapper.like(StringUtils.isNotBlank(req.getOrderNumber()), "t1.order_number", req.getOrderNumber());
        queryWrapper.orderByDesc("t1.create_time");
        return queryWrapper;
    }

    @Override
    public List<LicensesVO> queryListVo(LicensePageReq req) {
        return baseMapper.selectListVo(buildQueryWrapper(req));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean allocate(LicenseAllocationReq req) {
        // Decrease license seats
        LicensesEntity license = this.getById(req.getLicenseId());
        if (license != null) {
            int quantity = req.getQuantity() != null ? req.getQuantity() : 1;
            long currentSeats = license.getTotalSeats() != null ? license.getTotalSeats() : 0L;
            license.setTotalSeats(currentSeats - quantity);
            this.updateById(license);
        }

        return allocationsService.createAllocation(
                AssetType.LICENSE,
                req.getLicenseId(),
                req.getOwnerType(),
                req.getOwnerId(),
                req.getQuantity(),
                req.getNote()
        );
    }

    @Override
    public Page<LicenseAllocationVO> queryAllocations(Page<LicenseAllocationVO> page, Long licenseId) {
        return baseMapper.selectAllocationsPage(page, licenseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deallocate(Long allocationId) {
        AllocationsEntity allocation = allocationsService.getById(allocationId);
        if (allocation == null || !AllocationStatus.ACTIVE.equals(allocation.getStatus())) {
            return false;
        }

        // Increase license seats
        if (AssetType.LICENSE.equals(allocation.getItemType())) {
            LicensesEntity license = this.getById(allocation.getItemId());
            if (license != null) {
                int quantity = allocation.getQuantity() != null ? allocation.getQuantity() : 1;
                long currentSeats = license.getTotalSeats() != null ? license.getTotalSeats() : 0L;
                license.setTotalSeats(currentSeats + quantity);
                this.updateById(license);
            }
        }

        return allocationsService.closeAllocation(allocationId);
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        return baseMapper.selectCategoryStats();
    }

    @Override
    public Map<String, Object> getIndicatorStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysLater = now.plusDays(30);

        Map<String, Object> stats = new HashMap<>();

        // 1. 总数量
        stats.put("totalCount", this.count());

        // 2. 即将到期 (30天内)
        stats.put("expiringSoonCount", this.count(new LambdaQueryWrapper<LicensesEntity>()
                .gt(LicensesEntity::getExpirationDate, now)
                .le(LicensesEntity::getExpirationDate, thirtyDaysLater)));

        // 3. 已经到期
        stats.put("expiredCount", this.count(new LambdaQueryWrapper<LicensesEntity>()
                .lt(LicensesEntity::getExpirationDate, now)));

        // 4. 库存不足 (<1)
        stats.put("insufficientCount", this.count(new LambdaQueryWrapper<LicensesEntity>()
                .lt(LicensesEntity::getTotalSeats, 1)));

        return stats;
    }


    @Override
    public boolean save(LicensesEntity entity) {
        boolean result = super.save(entity);
        if (result && entity.getId() != null) {
            // 异步同步统计数据
            assetsMonthlyStatsService.saveOrUpdateStatsByAsset(AssetType.LICENSE, entity.getId());
        }
        return result;
    }

    @Override
    public boolean updateById(LicensesEntity entity) {
        boolean result = super.updateById(entity);
        if (result && entity.getId() != null) {
            // 重新计算当前月份的统计数据
            assetsMonthlyStatsService.recalculateAssetStats(AssetType.LICENSE, entity.getId(), LocalDate.now());
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            // 删除该资产的所有统计数据
            assetsMonthlyStatsService.deleteStatsByAsset(AssetType.LICENSE, (Long) id);
        }
        return result;
    }

    @Override
    public boolean removeLicensesByIds(List<Long> ids) {
        boolean result = super.removeByIds(ids);
        if (result) {
            for (Long id : ids) {
                // 删除该资产的所有统计数据
                assetsMonthlyStatsService.deleteStatsByAsset(AssetType.LICENSE, id);
            }
        }
        return result;
    }

}

