package com.ciyocloud.itam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.BaseEntity;
import com.ciyocloud.itam.enums.AssetType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资产月度价值统计 itam_device_monthly_stats
 *
 * @author codeck
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_device_monthly_stats")
public class AssetsMonthlyStatsEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 资产类型 (设备/耗材)
     */
    private AssetType assetsType;

    /**
     * 资产
     */
    private Long assetsId;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 初始价值(原值)
     */
    private BigDecimal initialValue;

    /**
     * 当前价值
     */
    private BigDecimal currentValue;

    /**
     * 累计折旧
     */
    private BigDecimal accumulatedDepreciation;

    /**
     * 统计月份 (YYYY-MM)
     */
    private String statsMonth;

    /**
     * 统计日期
     */
    private LocalDate statsDate;

}
