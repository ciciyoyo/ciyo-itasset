package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.itam.entity.StocktakeItemsEntity;
import com.ciyocloud.itam.entity.StocktakesEntity;
import com.ciyocloud.itam.enums.StocktakeItemStatus;
import com.ciyocloud.itam.enums.StocktakeStatus;
import com.ciyocloud.itam.mapper.StocktakeItemsMapper;
import com.ciyocloud.itam.mapper.StocktakesMapper;
import com.ciyocloud.itam.req.StocktakeItemsPageReq;
import com.ciyocloud.itam.service.DeviceService;
import com.ciyocloud.itam.service.StocktakeItemsService;
import com.ciyocloud.itam.vo.StocktakeItemsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 盘点明细Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Service
@RequiredArgsConstructor
public class StocktakeItemsServiceImpl extends ServiceImpl<StocktakeItemsMapper, StocktakeItemsEntity> implements StocktakeItemsService {

    private final DeviceService deviceService;
    private final StocktakesMapper stocktakesMapper;

    @Override
    public Page<StocktakeItemsVO> queryPageVo(Page<StocktakeItemsEntity> page, StocktakeItemsPageReq req) {
        QueryWrapper<StocktakeItemsEntity> queryWrapper = getWrapper(req);
        Page<StocktakeItemsVO> voPage = new Page<>(page.getCurrent(), page.getSize());
        return baseMapper.queryPageVo(voPage, queryWrapper);
    }

    @Override
    public List<StocktakeItemsVO> queryListVo(StocktakeItemsPageReq req) {
        QueryWrapper<StocktakeItemsEntity> queryWrapper = getWrapper(req);
        return baseMapper.queryListVo(queryWrapper);
    }

    private QueryWrapper<StocktakeItemsEntity> getWrapper(StocktakeItemsPageReq req) {
        QueryWrapper<StocktakeItemsEntity> queryWrapper = new QueryWrapper<>();
        if (req != null) {
            queryWrapper.eq(req.getStocktakeId() != null, "t1.stocktake_id", req.getStocktakeId())
                    .eq(req.getAssetId() != null, "t1.asset_id", req.getAssetId())
                    .eq(req.getStatus() != null, "t1.status", req.getStatus())
                    .eq(req.getScannedBy() != null, "t1.scanned_by", req.getScannedBy())
                    .eq(req.getExpectedLocationId() != null, "t1.expected_location_id", req.getExpectedLocationId())
                    .eq(req.getActualLocationId() != null, "t1.actual_location_id", req.getActualLocationId())
                    .like(StringUtils.hasText(req.getStocktakeName()), "t2.name", req.getStocktakeName())
                    .like(StringUtils.hasText(req.getAssetName()), "t3.name", req.getAssetName());
        }
        queryWrapper.orderByDesc("t1.create_time");
        return queryWrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStocktakeItem(StocktakeItemsEntity stocktakeItems) {
        stocktakeItems.setScannedBy(SecurityUtils.getUserId());
        stocktakeItems.setScannedAt(LocalDateTime.now());
        boolean updated = this.updateById(stocktakeItems);
        if (updated) {
            StocktakeItemsEntity item = this.getById(stocktakeItems.getId());
            if (item != null) {
                // 如果是报废，同步更新资产状态
                if (StocktakeItemStatus.SCRAPPED.equals(item.getStatus())) {
                    if (item.getAssetId() != null) {
                        deviceService.scrap(Collections.singletonList(item.getAssetId()));
                    }
                }

                // 检查是否所有明细都已完成
                Long stocktakeId = item.getStocktakeId();
                QueryWrapper<StocktakeItemsEntity> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("stocktake_id", stocktakeId)
                        .eq("status", StocktakeItemStatus.PENDING.getCode());
                long pendingCount = this.count(queryWrapper);

                if (pendingCount == 0) {
                    // 更新盘点任务状态为已完成
                    StocktakesEntity stocktake = new StocktakesEntity();
                    stocktake.setId(stocktakeId);
                    stocktake.setStatus(StocktakeStatus.FINISHED);
                    stocktake.setEndDate(LocalDateTime.now());
                    stocktakesMapper.updateById(stocktake);
                }
            }
        }
        return updated;
    }
}
