package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.BaseEntity;
import com.ciyocloud.excel.convert.DictEnumConvert;
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
@ExcelIgnoreUnannotated
public class AssetsMonthlyStatsEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 资产类型 (设备/耗材)
     */
    @ExcelProperty(value = "资产类型", converter = DictEnumConvert.class)
    private AssetType assetsType;

    /**
     * 资产
     */
    @ExcelProperty(value = "资产ID")
    private Long assetsId;

    /**
     * 分类 ID
     */
    @ExcelProperty(value = "分类ID")
    private Long categoryId;

    /**
     * 初始价值(原值)
     */
    @ExcelProperty(value = "初始价值")
    private BigDecimal initialValue;

    /**
     * 当前价值
     */
    @ExcelProperty(value = "当前价值")
    private BigDecimal currentValue;

    /**
     * 累计折旧
     */
    @ExcelProperty(value = "累计折旧")
    private BigDecimal accumulatedDepreciation;

    /**
     * 统计月份 (YYYY-MM)
     */
    @ExcelProperty(value = "统计月份")
    private String statsMonth;

    /**
     * 统计日期
     */
    @ExcelProperty(value = "统计日期")
    private LocalDate statsDate;

}
