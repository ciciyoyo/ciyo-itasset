package com.ciyocloud.itam.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产指标统计 VO
 *
 * @author codeck
 * @since 2026-01-01
 */
@Data
public class AssetsSummaryVO {

    /**
     * 总资产金额
     */
    private BigDecimal totalAssetAmount = BigDecimal.ZERO;

    /**
     * 总资产数（各项资产数量之和）
     */
    private Long totalAssetCount = 0L;

    /**
     * 总设备数
     */
    private Long totalDevices = 0L;

    /**
     * 总软件（许可证）数
     */
    private Long totalSoftware = 0L;

    /**
     * 总配件数
     */
    private Long totalAccessories = 0L;

    /**
     * 总服务数
     */
    private Long totalServices = 0L;

    /**
     * 总耗材数
     */
    private Long totalConsumables = 0L;
}
