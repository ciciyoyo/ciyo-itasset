package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.mybatis.service.BaseServiceImpl;
import com.ciyocloud.common.sse.util.SseAsyncProcessUtils;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.OfferingEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.enums.OfferingStatus;
import com.ciyocloud.itam.listener.OfferingImportListener;
import com.ciyocloud.itam.mapper.OfferingMapper;
import com.ciyocloud.itam.req.OfferingPageReq;
import com.ciyocloud.itam.service.AssetsMonthlyStatsService;
import com.ciyocloud.itam.service.AssetsReportService;
import com.ciyocloud.itam.service.OfferingService;
import com.ciyocloud.itam.service.SuppliersService;
import com.ciyocloud.itam.vo.OfferingVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务 Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OfferingServiceImpl extends BaseServiceImpl<OfferingMapper, OfferingEntity> implements OfferingService {

    private final SuppliersService suppliersService;
    private final AssetsMonthlyStatsService assetsMonthlyStatsService;
    private final AssetsReportService assetsReportService;


    @Override
    public Page<OfferingVO> queryPageVo(Page<OfferingVO> page, OfferingPageReq req) {
        QueryWrapper<OfferingEntity> wrapper = getWrapper(req);
        Page<OfferingVO> voPage = this.baseMapper.selectOfferingPage(page, wrapper);
        return voPage;
    }

    @Override
    public List<OfferingVO> queryListVo(OfferingPageReq req) {
        QueryWrapper<OfferingEntity> wrapper = getWrapper(req);
        List<OfferingVO> list = this.baseMapper.selectOfferingList(wrapper);
        return list;
    }

    private QueryWrapper<OfferingEntity> getWrapper(OfferingPageReq req) {
        QueryWrapper<OfferingEntity> wrapper = new QueryWrapper<>();
        if (req.getQuickSearchType() != null) {
            if (req.getQuickSearchType() == 2) {
                // 异常的服务
                wrapper.eq("t1.offering_status", OfferingStatus.EXCEPTION);
            }
        }

        wrapper.like(StringUtils.hasText(req.getName()), "t1.name", req.getName());
        wrapper.like(StringUtils.hasText(req.getServiceNumber()), "t1.service_number", req.getServiceNumber());
        wrapper.eq(req.getOfferingStatus() != null, "t1.offering_status", req.getOfferingStatus());
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
        return vo;
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

    @Override
    public void importData(InputStream inputStream, String originalFilename, String progressKey, Long userId) {
        try {
            log.info("开始导入服务数据，progressKey: {}, userId: {}, 文件名: {}", progressKey, userId, originalFilename);

            // 初始化进度
            SseAsyncProcessUtils.setTips("正在解析Excel文件...");

            // 创建支持SSE进度推送的导入监听器
            OfferingImportListener listener = new OfferingImportListener(progressKey, userId);
            // 更新进度：开始导入
            SseAsyncProcessUtils.setProcess(0, "开始导入服务数据...");
            //执行导入
            ExcelUtils.importExcel(inputStream, OfferingVO.class, listener);
        } catch (Exception e) {
            log.error("服务数据导入失败，progressKey: {}, 错误: {}", progressKey, e.getMessage(), e);
            SseAsyncProcessUtils.setError("导入失败: " + e.getMessage());
            throw e;
        }
    }

}
