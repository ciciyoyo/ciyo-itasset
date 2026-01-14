package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.itam.entity.AccessoriesEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.mapper.AccessoriesMapper;
import com.ciyocloud.itam.service.AccessoriesService;
import com.ciyocloud.itam.service.AssetsMonthlyStatsService;
import com.ciyocloud.itam.vo.AccessoriesVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 配件Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 20:10:26
 */
@Service
@RequiredArgsConstructor
public class AccessoriesServiceImpl extends ServiceImpl<AccessoriesMapper, AccessoriesEntity> implements AccessoriesService {

    private final AssetsMonthlyStatsService assetsMonthlyStatsService;

    @Override
    public Page<AccessoriesVO> queryPageVo(Page<AccessoriesEntity> page, Wrapper<AccessoriesEntity> queryWrapper) {
        Page<AccessoriesVO> voPage = new Page<>(page.getCurrent(), page.getSize());
        return baseMapper.selectPageVo(voPage, queryWrapper);
    }

    @Override
    public List<AccessoriesVO> queryListVo(Wrapper<AccessoriesEntity> queryWrapper) {
        return baseMapper.selectListVo(queryWrapper);
    }

    @Override
    public Map<String, Object> getSummaryStats() {
        LocalDate now = LocalDate.now();
        LocalDate soonDate = now.plusDays(30);
        return baseMapper.selectSummaryStats(now, soonDate);
    }


    @Override
    public boolean save(AccessoriesEntity entity) {
        boolean result = super.save(entity);
        if (result && entity.getId() != null) {
            // 异步同步统计数据
            assetsMonthlyStatsService.saveOrUpdateStatsByAsset(AssetType.ACCESSORY, entity.getId());
        }
        return result;
    }

    @Override
    public boolean updateById(AccessoriesEntity entity) {
        boolean result = super.updateById(entity);
        if (result && entity.getId() != null) {
            // 重新计算当前月份的统计数据
            assetsMonthlyStatsService.recalculateAssetStats(AssetType.ACCESSORY, entity.getId(), LocalDate.now());
        }
        return result;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAccessoriesByIds(List<Long> ids) {
        boolean result = super.removeByIds(ids);
        if (result) {
            for (Long id : ids) {
                // 删除该资产的所有统计数据
                assetsMonthlyStatsService.deleteStatsByAsset(AssetType.ACCESSORY, id);
            }
        }
        return result;
    }

}
