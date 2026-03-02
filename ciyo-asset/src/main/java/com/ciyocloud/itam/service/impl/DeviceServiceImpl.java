package com.ciyocloud.itam.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.mybatis.service.BaseServiceImpl;
import com.ciyocloud.itam.entity.DeviceEntity;
import com.ciyocloud.itam.entity.ModelsEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.enums.DeviceStatus;
import com.ciyocloud.itam.listener.DeviceImportListener;
import com.ciyocloud.itam.mapper.DeviceMapper;
import com.ciyocloud.itam.req.DevicePageReq;
import com.ciyocloud.itam.service.AssetsMonthlyStatsService;
import com.ciyocloud.itam.service.DeviceService;
import com.ciyocloud.itam.service.ModelsService;
import com.ciyocloud.itam.util.AssetCodeUtils;
import com.ciyocloud.itam.vo.DeviceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 设备管理Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends BaseServiceImpl<DeviceMapper, DeviceEntity> implements DeviceService {

    private final AssetsMonthlyStatsService assetsMonthlyStatsService;
    private final AssetCodeUtils assetCodeUtils;
    private final ModelsService modelsService;

    @Override
    public Page<DeviceVO> queryPageVo(Page<DeviceEntity> page, DevicePageReq req) {
        QueryWrapper<DeviceEntity> queryWrapper = getDeviceVOQueryWrapper(req);
        Page<DeviceVO> voPage = new Page<>(page.getCurrent(), page.getSize());
        return baseMapper.selectPageVo(voPage, queryWrapper);
    }

    @Override
    public List<DeviceVO> queryListVo(DevicePageReq req) {
        QueryWrapper<DeviceEntity> queryWrapper = getDeviceVOQueryWrapper(req);
        return baseMapper.selectListVo(queryWrapper);
    }

    private QueryWrapper<DeviceEntity> getDeviceVOQueryWrapper(DevicePageReq req) {
        QueryWrapper<DeviceEntity> queryWrapper = new QueryWrapper<>();
        if (req != null) {
            queryWrapper.eq(req.getModelId() != null, "t1.model_id", req.getModelId())
                    .eq("t1.deleted", 0)
                    .eq(req.getAssetsStatus() != null, "t1.assets_status", req.getAssetsStatus())
                    .like(StringUtils.hasText(req.getName()), "t1.name", req.getName())
                    .eq(StringUtils.hasText(req.getSerial()), "t1.serial", req.getSerial())
                    .eq(StringUtils.hasText(req.getDeviceNo()), "t1.device_no", req.getDeviceNo())
                    .eq(StringUtils.hasText(req.getAssetTag()), "t1.asset_tag", req.getAssetTag())
                    .eq(req.getAssignedTo() != null, "t1.assigned_to", req.getAssignedTo())
                    .like(StringUtils.hasText(req.getAssignedToName()), "u.nick_name", req.getAssignedToName())
                    .eq(req.getLocationId() != null, "t1.location_id", req.getLocationId())
                    .eq(req.getSupplierId() != null, "t1.supplier_id", req.getSupplierId())
                    .eq(req.getDepreciationId() != null, "t1.depreciation_id", req.getDepreciationId());
        }
        queryWrapper.orderByDesc("t1.create_time");
        return queryWrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean scrap(List<Long> ids) {
        List<DeviceEntity> list = new ArrayList<>();
        for (Long id : ids) {
            DeviceEntity entity = new DeviceEntity();
            entity.setId(id);
            entity.setAssetsStatus(DeviceStatus.SCRAPPED);
            list.add(entity);
        }
        boolean result = updateBatchById(list);
        if (result) {
            for (Long id : ids) {
                // 重新计算当前月份的统计数据
                assetsMonthlyStatsService.recalculateAssetStats(AssetType.DEVICE, id, LocalDate.now());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean recover(List<Long> ids) {
        List<DeviceEntity> list = new ArrayList<>();
        for (Long id : ids) {
            DeviceEntity entity = new DeviceEntity();
            entity.setId(id);
            entity.setAssetsStatus(DeviceStatus.PENDING);
            list.add(entity);
        }
        boolean result = updateBatchById(list);
        if (result) {
            for (Long id : ids) {
                // 重新计算当前月份的统计数据
                assetsMonthlyStatsService.recalculateAssetStats(AssetType.DEVICE, id, LocalDate.now());
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> getSummaryStats() {
        LocalDate now = LocalDate.now();
        LocalDate soonDate = now.plusDays(30);
        return baseMapper.selectSummaryStats(now, soonDate);
    }


    @Override
    public boolean save(DeviceEntity entity) {
        if (StrUtil.isBlank(entity.getDeviceNo())) {
            // 自动生成
            ModelsEntity model = modelsService.getById(entity.getModelId());
            if (model == null) {
                throw new RuntimeException("关联型号不存在");
            }
            entity.setDeviceNo(assetCodeUtils.generate(model.getCategoryId()));
        } else {
            // 查重
            long count = count(new QueryWrapper<DeviceEntity>()
                    .eq("device_no", entity.getDeviceNo())
                    .ne(entity.getId() != null, "id", entity.getId()));
            if (count > 0) {
                throw new RuntimeException("设备编号已存在");
            }
        }

        boolean result = super.save(entity);
        if (result && entity.getId() != null) {
            // 异步同步统计数据
            assetsMonthlyStatsService.saveOrUpdateStatsByAsset(AssetType.DEVICE, entity.getId());
        }
        return result;
    }

    @Override
    public boolean updateById(DeviceEntity entity) {
        if (StrUtil.isNotBlank(entity.getDeviceNo())) {
            // 查重
            long count = count(new QueryWrapper<DeviceEntity>()
                    .eq("device_no", entity.getDeviceNo())
                    .ne(entity.getId() != null, "id", entity.getId()));
            if (count > 0) {
                throw new RuntimeException("设备编号已存在");
            }
        }

        boolean result = super.updateById(entity);
        if (result && entity.getId() != null) {
            // 重新计算当前月份的统计数据
            assetsMonthlyStatsService.recalculateAssetStats(AssetType.DEVICE, entity.getId(), LocalDate.now());
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            // 删除该资产的所有统计数据
            assetsMonthlyStatsService.deleteStatsByAsset(AssetType.DEVICE, (Long) id);
        }
        return result;
    }


    @Override
    public boolean removeDevicesByIds(List<Long> ids) {
        boolean result = super.removeByIds(ids);
        if (result) {
            for (Long id : ids) {
                // 删除该资产的所有统计数据
                assetsMonthlyStatsService.deleteStatsByAsset(AssetType.DEVICE, id);
            }
        }
        return result;
    }

    @Override
    public void importDevices(String progressKey, InputStream inputStream, Long userId) {
        DeviceImportListener listener = new DeviceImportListener(progressKey, userId);
        EasyExcel.read(inputStream, DeviceVO.class, listener)
                .sheet()
                .doRead();
    }

}
