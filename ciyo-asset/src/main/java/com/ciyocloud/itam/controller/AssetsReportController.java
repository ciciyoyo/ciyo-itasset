package com.ciyocloud.itam.controller;

import com.ciyocloud.common.util.Result;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.service.AssetsReportService;
import com.ciyocloud.itam.vo.AssetsReportVO;
import com.ciyocloud.itam.vo.AssetsSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资产报表统计
 *
 * @author codeck
 * @since 2026-01-01
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/reports")
public class AssetsReportController {

    private final AssetsReportService assetsReportService;

    /**
     * 获取当前年份内各类资产的月度价值统计
     */
    @PreAuthorize("@ss.hasPermi('itam:reports:query')")
    @GetMapping("/yearly-assets-value")
    public Result<List<AssetsReportVO>> getYearlyAssetsValueStats() {
        return Result.success(assetsReportService.getYearlyAssetsValueStats());
    }

    /**
     * 获取指定资产类型的年度月度趋势统计（折线图展示）
     */
    @PreAuthorize("@ss.hasPermi('itam:reports:query')")
    @GetMapping("/monthly-trend")
    public Result<List<AssetsReportVO>> getMonthlyTrend(AssetType assetsType) {
        return Result.success(assetsReportService.getMonthlyTrend(assetsType));
    }

    /**
     * 获取最新各资产分类的价值分布（饼图展示）
     */
    @PreAuthorize("@ss.hasPermi('itam:reports:query')")
    @GetMapping("/distribution")
    public Result<List<AssetsReportVO>> getAssetsDistribution() {
        return Result.success(assetsReportService.getAssetsDistribution());
    }

    /**
     * 获取资产指标统计摘要
     */
    @PreAuthorize("@ss.hasPermi('itam:reports:query')")
    @GetMapping("/summary")
    public Result<AssetsSummaryVO> getAssetsSummary() {
        return Result.success(assetsReportService.getAssetsSummary());
    }

    /**
     * 获取某类资产最新总价值
     */
    @PreAuthorize("@ss.hasPermi('itam:reports:query')")
    @GetMapping("/latest-value")
    public Result<BigDecimal> getLatestAssetValue(AssetType assetsType) {
        return Result.success(assetsReportService.getLatestAssetValue(assetsType));
    }
}
