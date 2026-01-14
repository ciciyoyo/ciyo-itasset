package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.itam.entity.DeviceEntity;
import com.ciyocloud.itam.entity.ModelsEntity;
import com.ciyocloud.itam.entity.StocktakeItemsEntity;
import com.ciyocloud.itam.entity.StocktakesEntity;
import com.ciyocloud.itam.enums.StocktakeItemStatus;
import com.ciyocloud.itam.mapper.StocktakesMapper;
import com.ciyocloud.itam.req.StocktakesPageReq;
import com.ciyocloud.itam.service.DeviceService;
import com.ciyocloud.itam.service.ModelsService;
import com.ciyocloud.itam.service.StocktakeItemsService;
import com.ciyocloud.itam.service.StocktakesService;
import com.ciyocloud.itam.vo.StocktakesVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public Page<StocktakesVO> queryPageVo(Page<StocktakesEntity> page, StocktakesPageReq req) {
        Page<StocktakesVO> voPage = new Page<>(page.getCurrent(), page.getSize());
        LambdaQueryWrapper<StocktakesEntity> wrapper = getWrapper(req);
        return baseMapper.selectPageVo(voPage, wrapper);
    }

    @Override
    public List<StocktakesVO> queryListVo(StocktakesPageReq req) {
        LambdaQueryWrapper<StocktakesEntity> wrapper = getWrapper(req);
        return baseMapper.selectListVo(wrapper);
    }

    private LambdaQueryWrapper<StocktakesEntity> getWrapper(StocktakesPageReq req) {
        LambdaQueryWrapper<StocktakesEntity> wrapper = Wrappers.lambdaQuery();
        if (req != null) {
            wrapper.like(StringUtils.isNotBlank(req.getName()), StocktakesEntity::getName, req.getName())
                    .eq(req.getStatus() != null, StocktakesEntity::getStatus, req.getStatus())
                    .eq(req.getLocationId() != null, StocktakesEntity::getLocationId, req.getLocationId())
                    .eq(req.getCategoryId() != null, StocktakesEntity::getCategoryId, req.getCategoryId())
                    .eq(req.getManagerId() != null, StocktakesEntity::getManagerId, req.getManagerId())
                    .ge(req.getStartDate() != null, StocktakesEntity::getStartDate, req.getStartDate())
                    .le(req.getEndDate() != null, StocktakesEntity::getEndDate, req.getEndDate());
        }
        wrapper.orderByDesc(StocktakesEntity::getCreateTime);
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
        LambdaQueryWrapper<DeviceEntity> queryWrapper = Wrappers.lambdaQuery();
        // 如果指定了位置
        queryWrapper.eq(stocktakes.getLocationId() != null, DeviceEntity::getLocationId, stocktakes.getLocationId());

        // 如果指定了分类，需要先查询该分类下的型号
        if (stocktakes.getCategoryId() != null) {
            List<Long> modelIds = modelsService.list(Wrappers.<ModelsEntity>lambdaQuery()
                            .eq(ModelsEntity::getCategoryId, stocktakes.getCategoryId()))
                    .stream().map(ModelsEntity::getId).collect(Collectors.toList());

            if (modelIds.isEmpty()) {
                // 该分类下没有型号，也就没有资产，直接返回成功
                return true;
            }
            queryWrapper.in(DeviceEntity::getModelId, modelIds);
        }

        List<DeviceEntity> assetsList = deviceService.list(queryWrapper);

        if (assetsList.isEmpty()) {
            return true;
        }

        // 3. 生成盘点明细
        List<StocktakeItemsEntity> itemsList = new ArrayList<>();
        for (DeviceEntity asset : assetsList) {
            StocktakeItemsEntity item = new StocktakeItemsEntity();
            item.setStocktakeId(stocktakes.getId());
            item.setAssetId(asset.getId());
            item.setStatus(StocktakeItemStatus.PENDING);
            item.setExpectedLocationId(asset.getLocationId());
            itemsList.add(item);
        }

        return stocktakeItemsService.saveBatch(itemsList);
    }

}
