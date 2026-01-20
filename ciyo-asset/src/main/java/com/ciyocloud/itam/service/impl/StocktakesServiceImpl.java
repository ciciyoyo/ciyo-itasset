package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.itam.entity.*;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.enums.StocktakeItemStatus;
import com.ciyocloud.itam.mapper.StocktakesMapper;
import com.ciyocloud.itam.req.StocktakesPageReq;
import com.ciyocloud.itam.service.*;
import com.ciyocloud.itam.vo.StocktakesDetailVO;
import com.ciyocloud.itam.vo.StocktakesVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 盘点任务Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Service
@RequiredArgsConstructor
public class StocktakesServiceImpl extends ServiceImpl<StocktakesMapper, StocktakesEntity> implements StocktakesService {

    private final DeviceService deviceService;
    private final ModelsService modelsService;
    private final StocktakeItemsService stocktakeItemsService;
    private final CategoriesService categoriesService;
    private final AccessoriesService accessoriesService;
    private final ConsumablesService consumablesService;

    @Override
    public Page<StocktakesVO> queryPageVo(Page<StocktakesEntity> page, StocktakesPageReq req) {
        Page<StocktakesVO> voPage = new Page<>(page.getCurrent(), page.getSize());
        QueryWrapper<StocktakesEntity> wrapper = getWrapper(req);
        return baseMapper.selectPageVo(voPage, wrapper);
    }

    @Override
    public List<StocktakesVO> queryListVo(StocktakesPageReq req) {
        QueryWrapper<StocktakesEntity> wrapper = getWrapper(req);
        return baseMapper.selectListVo(wrapper);
    }

    private QueryWrapper<StocktakesEntity> getWrapper(StocktakesPageReq req) {
        QueryWrapper<StocktakesEntity> wrapper = Wrappers.query();
        if (req != null) {
            wrapper.like(StringUtils.isNotBlank(req.getName()), "t1.name", req.getName()).eq(req.getStatus() != null, "t1.status", req.getStatus()).eq(req.getLocationId() != null, "t1.location_id", req.getLocationId()).eq(req.getCategoryId() != null, "t1.category_id", req.getCategoryId()).eq(req.getManagerId() != null, "t1.manager_id", req.getManagerId()).ge(req.getStartDate() != null, "t1.start_date", req.getStartDate()).le(req.getEndDate() != null, "t1.end_date", req.getEndDate());
        }
        wrapper.orderByDesc("t1.create_time");
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(StocktakesEntity stocktakes) {
        // 1. 保存盘点任务
        boolean saved = this.save(stocktakes);
        if (!saved) {
            return false;
        }

        // 2. 查询需要盘点的资产
        List<StocktakeItemsEntity> itemsList = new ArrayList<>();
        Long categoryId = stocktakes.getCategoryId();
        Long locationId = stocktakes.getLocationId();

        if (categoryId != null) {
            CategoriesEntity category = categoriesService.getById(categoryId);
            if (category != null && null != category.getCategoryType()) {
                if (AssetType.DEVICE.equals(category.getCategoryType())) {
                    // 查询该分类下的型号
                    List<DeviceEntity> deviceList = deviceService.list(Wrappers.<DeviceEntity>lambdaQuery().eq(locationId != null, DeviceEntity::getLocationId, locationId).in(DeviceEntity::getCategoryId, categoryId));
                    for (DeviceEntity device : deviceList) {
                        itemsList.add(createStocktakeItem(stocktakes.getId(), device.getId(), device.getLocationId()));
                    }
                } else if (AssetType.ACCESSORY.equals(category.getCategoryType())) {
                    List<AccessoriesEntity> accessoriesList = accessoriesService.list(Wrappers.<AccessoriesEntity>lambdaQuery().eq(AccessoriesEntity::getCategoryId, categoryId).eq(locationId != null, AccessoriesEntity::getLocationId, locationId));

                    for (AccessoriesEntity accessory : accessoriesList) {
                        itemsList.add(createStocktakeItem(stocktakes.getId(), accessory.getId(), accessory.getLocationId()));
                    }
                } else if (AssetType.CONSUMABLE.equals(category.getCategoryType())) {
                    List<ConsumablesEntity> consumablesList = consumablesService.list(Wrappers.<ConsumablesEntity>lambdaQuery().eq(ConsumablesEntity::getCategoryId, categoryId).eq(locationId != null, ConsumablesEntity::getLocationId, locationId));

                    for (ConsumablesEntity consumable : consumablesList) {
                        itemsList.add(createStocktakeItem(stocktakes.getId(), consumable.getId(), consumable.getLocationId()));
                    }
                }
            }
        } else {
            // 如果未指定分类，默认盘点设备 (保持原有逻辑)
            List<DeviceEntity> deviceList = deviceService.list(Wrappers.<DeviceEntity>lambdaQuery().eq(locationId != null, DeviceEntity::getLocationId, locationId));

            for (DeviceEntity device : deviceList) {
                itemsList.add(createStocktakeItem(stocktakes.getId(), device.getId(), device.getLocationId()));
            }
        }

        if (itemsList.isEmpty()) {
            return true;
        }

        // 3. 生成盘点明细
        return stocktakeItemsService.saveBatch(itemsList);
    }

    private StocktakeItemsEntity createStocktakeItem(Long stocktakeId, Long assetId, Long locationId) {
        StocktakeItemsEntity item = new StocktakeItemsEntity();
        item.setStocktakeId(stocktakeId);
        item.setAssetId(assetId);
        item.setStatus(StocktakeItemStatus.PENDING);
        item.setExpectedLocationId(locationId);
        item.setScannedBy(SecurityUtils.getUserId());
        item.setScannedAt(LocalDateTime.now());
        return item;
    }

    @Override
    public StocktakesDetailVO getDetailVo(Long id) {
        QueryWrapper<StocktakesEntity> wrapper = Wrappers.query();
        wrapper.eq("t1.id", id);
        List<StocktakesVO> list = baseMapper.selectListVo(wrapper);
        if (list.isEmpty()) {
            return null;
        }
        StocktakesVO vo = list.get(0);
        StocktakesDetailVO detailVo = new StocktakesDetailVO();
        org.springframework.beans.BeanUtils.copyProperties(vo, detailVo);

        // 统计数量
        QueryWrapper<StocktakeItemsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status", "count(*) as count")
                .eq("stocktake_id", id)
                .groupBy("status");

        List<Map<String, Object>> stats = stocktakeItemsService.listMaps(queryWrapper);

        Map<String, Long> countMap = stats.stream()
                .filter(m -> m.get("status") != null)
                .collect(Collectors.toMap(
                        m -> String.valueOf(m.get("status")),
                        m -> ((Number) m.get("count")).longValue()
                ));

        detailVo.setTotalCount(countMap.values().stream().mapToLong(Long::longValue).sum());
        detailVo.setUncheckedCount(countMap.getOrDefault(StocktakeItemStatus.PENDING.getCode(), 0L));
        detailVo.setDeficitCount(countMap.getOrDefault(StocktakeItemStatus.LOST.getCode(), 0L));
        detailVo.setSurplusCount(countMap.getOrDefault(StocktakeItemStatus.SURPLUS.getCode(), 0L));
        detailVo.setNormalCount(countMap.getOrDefault(StocktakeItemStatus.NORMAL.getCode(), 0L));
        detailVo.setDamagedCount(countMap.getOrDefault(StocktakeItemStatus.DAMAGED.getCode(), 0L));
        detailVo.setScrappedCount(countMap.getOrDefault(StocktakeItemStatus.SCRAPPED.getCode(), 0L));

        return detailVo;
    }
}
