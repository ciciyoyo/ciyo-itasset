package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.itam.entity.AssetsMonthlyStatsEntity;
import com.ciyocloud.itam.enums.AssetType;

import java.time.LocalDate;

/**
 * 资产月度价值统计 Service 接口
 *
 * @author codeck
 * @since 2026-01-01
 */
public interface AssetsMonthlyStatsService extends IService<AssetsMonthlyStatsEntity> {

    /**
     * 计算并保存当天的资产价值统计
     * 建议每日凌晨运行
     *
     * @param date 指定计算日期,通常为当天
     */
    void calculateDailyStats(LocalDate date);

    /**
     * 根据资产类型和ID删除该资产的所有统计数据
     *
     * @param assetsType 资产类型
     * @param assetsId   资产ID
     */
    void deleteStatsByAsset(AssetType assetsType, Long assetsId);

    /**
     * 根据资产类型和ID异步保存或更新该资产的统计数据
     * 会查询资产信息并计算当前价值
     *
     * @param assetsType 资产类型
     * @param assetsId   资产ID
     */
    void saveOrUpdateStatsByAsset(AssetType assetsType, Long assetsId);

    /**
     * 重新计算指定资产的统计数据(指定日期)
     *
     * @param assetsType 资产类型
     * @param assetsId   资产ID
     * @param date       统计日期
     */
    void recalculateAssetStats(AssetType assetsType, Long assetsId, LocalDate date);

}
