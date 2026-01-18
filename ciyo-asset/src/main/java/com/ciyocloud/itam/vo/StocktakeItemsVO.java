package com.ciyocloud.itam.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ciyocloud.itam.entity.StocktakeItemsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 盘点明细 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ExcelIgnoreUnannotated
public class StocktakeItemsVO extends StocktakeItemsEntity {

    /**
     * 盘点任务名称
     */
    @ExcelProperty(value = "盘点任务名称")
    private String stocktakeName;

    /**
     * 资产名称
     */
    @ExcelProperty(value = "资产名称")
    private String assetName;

    /**
     * 资产编码
     */
    @ExcelProperty(value = "资产编码")
    private String assetCode;

    /**
     * 系统记录位置名称
     */
    @ExcelProperty(value = "系统记录位置名称")
    private String expectedLocationName;

    /**
     * 实际发现位置名称
     */
    @ExcelProperty(value = "实际发现位置名称")
    private String actualLocationName;

    /**
     * 扫码人员名称
     */
    @ExcelProperty(value = "扫码人员名称")
    private String scannedByName;
}

