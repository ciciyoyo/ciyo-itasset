package com.ciyocloud.itam.service;

import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.vo.AssetsReportVO;
import com.ciyocloud.itam.vo.AssetsSummaryVO;
import com.ciyocloud.itam.vo.SupplierFailureStatsVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资产报表统计 Service 接口
 *
 * @author codeck
 * @since 2026-01-01
 */
public interface AssetsReportService {

    /**
     * 获取当前年份内各类资产的月度价值统计
     *
     * @return 统计列表
     */
    List<AssetsReportVO> getYearlyAssetsValueStats();

    /**
     * 获取指定资产类型的年度月度趋势
     *
     * @param assetsType 资产类型
     * @return 趋势列表
     */
    List<AssetsReportVO> getMonthlyTrend(AssetType assetsType);

    /**
     * 获取资产分布统计（饼图）
     *
     * @return 分布列表
     */
    List<AssetsReportVO> getAssetsDistribution();

    /**
     * 获取资产指标统计摘要
     *
     * @return 统计摘要
     */
    AssetsSummaryVO getAssetsSummary();

    /**
     * 统计不同服务商提供的服务出现异常的数量
     *
     * @return 统计列表
     */
    List<SupplierFailureStatsVO> getSupplierFailureStats();

    /**
     * 获取某类资产最新总价值
     *
     * @param assetsType 资产类型
     * @return 最新总价值
     */
    BigDecimal getLatestAssetValue(AssetType assetsType);
}
