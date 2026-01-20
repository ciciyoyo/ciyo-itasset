package com.ciyocloud.itam.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 盘点任务详情VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StocktakesDetailVO extends StocktakesVO {

    /**
     * 盘点总数
     */
    @ExcelProperty(value = "盘点总数")
    private Long totalCount;

    /**
     * 盘盈数量
     */
    @ExcelProperty(value = "盘盈数量")
    private Long surplusCount;

    /**
     * 盘亏数量
     */
    @ExcelProperty(value = "盘亏数量")
    private Long deficitCount;

    /**
     * 未盘数量
     */
    @ExcelProperty(value = "未盘数量")
    private Long uncheckedCount;

    /**
     * 正常数量
     */
    @ExcelProperty(value = "正常数量")
    private Long normalCount;

    /**
     * 损坏数量
     */
    @ExcelProperty(value = "损坏数量")
    private Long damagedCount;

    /**
     * 报废数量
     */
    @ExcelProperty(value = "报废数量")
    private Long scrappedCount;
}
