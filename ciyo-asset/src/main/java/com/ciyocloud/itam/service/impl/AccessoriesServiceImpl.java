package com.ciyocloud.itam.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.mybatis.service.BaseServiceImpl;
import com.ciyocloud.common.sse.util.SseAsyncProcessUtils;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.AccessoriesEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.listener.AccessoriesImportListener;
import com.ciyocloud.itam.mapper.AccessoriesMapper;
import com.ciyocloud.itam.req.AccessoriesPageReq;
import com.ciyocloud.itam.service.AccessoriesService;
import com.ciyocloud.itam.service.AssetsMonthlyStatsService;
import com.ciyocloud.itam.util.AssetCodeUtils;
import com.ciyocloud.itam.vo.AccessoriesVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 配件Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 20:10:26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessoriesServiceImpl extends BaseServiceImpl<AccessoriesMapper, AccessoriesEntity> implements AccessoriesService {

    private final AssetsMonthlyStatsService assetsMonthlyStatsService;
    private final AssetCodeUtils assetCodeUtils;

    @Override
    public Page<AccessoriesVO> queryPage(PageRequest pageReq, AccessoriesPageReq req) {
        QueryWrapper<AccessoriesEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("t1.deleted", 0);
        wrapper.like(StrUtil.isNotBlank(req.getName()), "t1.name", req.getName());
        wrapper.eq(ObjectUtil.isNotNull(req.getSupplierId()), "t1.supplier_id", req.getSupplierId());

        // 映射 assetTag -> assetNumber
        wrapper.like(StrUtil.isNotBlank(req.getAssetTag()), "t1.asset_number", req.getAssetTag());

        // 新增常用字段查询
        wrapper.eq(ObjectUtil.isNotNull(req.getCategoryId()), "t1.category_id", req.getCategoryId());
        wrapper.eq(ObjectUtil.isNotNull(req.getManufacturerId()), "t1.manufacturer_id", req.getManufacturerId());
        wrapper.eq(ObjectUtil.isNotNull(req.getLocationId()), "t1.location_id", req.getLocationId());

        wrapper.orderByDesc("t1.create_time");

        return baseMapper.selectPageVo(pageReq.toMpPage(), wrapper);
    }

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
        if (StrUtil.isBlank(entity.getAssetNumber())) {
            // 自动生成
            entity.setAssetNumber(assetCodeUtils.generate(entity.getCategoryId()));
        } else {
            // 查重
            long count = count(new QueryWrapper<AccessoriesEntity>()
                    .eq("asset_number", entity.getAssetNumber())
                    .ne(entity.getId() != null, "id", entity.getId()));
            if (count > 0) {
                throw new RuntimeException("资产编号已存在");
            }
        }

        boolean result = super.save(entity);
        if (result && entity.getId() != null) {
            // 异步同步统计数据
            assetsMonthlyStatsService.saveOrUpdateStatsByAsset(AssetType.ACCESSORY, entity.getId());
        }
        return result;
    }

    @Override
    public boolean updateById(AccessoriesEntity entity) {
        if (StrUtil.isNotBlank(entity.getAssetNumber())) {
            // 查重
            long count = count(new QueryWrapper<AccessoriesEntity>()
                    .eq("asset_number", entity.getAssetNumber())
                    .ne(entity.getId() != null, "id", entity.getId()));
            if (count > 0) {
                throw new RuntimeException("资产编号已存在");
            }
        }

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

    @Override
    public void importData(InputStream inputStream, String originalFilename, String progressKey, Long userId) {
        try {
            log.info("开始导入配件数据，progressKey: {}, userId: {}, 文件名: {}", progressKey, userId, originalFilename);

            // 初始化进度
            SseAsyncProcessUtils.setTips("正在解析Excel文件...");

            // 创建支持SSE进度推送的导入监听器
            AccessoriesImportListener listener = new AccessoriesImportListener(progressKey, userId);
            // 更新进度：开始导入
            SseAsyncProcessUtils.setProcess(0, "开始导入配件数据...");
            //执行导入
            ExcelUtils.importExcel(inputStream, AccessoriesVO.class, listener);
        } catch (Exception e) {
            log.error("配件数据导入失败，progressKey: {}, 错误: {}", progressKey, e.getMessage(), e);
            SseAsyncProcessUtils.setError("导入失败: " + e.getMessage());
            throw e;
        }
    }

}
