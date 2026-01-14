package com.ciyocloud.itam.vo;

import com.ciyocloud.itam.entity.StocktakeItemsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 盘点明细 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StocktakeItemsVO extends StocktakeItemsEntity {

    /**
     * 盘点任务名称
     */
    private String stocktakeName;

    /**
     * 资产名称
     */
    private String assetName;

    /**
     * 资产编码
     */
    private String assetCode;

    /**
     * 系统记录位置名称
     */
    private String expectedLocationName;

    /**
     * 实际发现位置名称
     */
    private String actualLocationName;

    /**
     * 扫码人员名称
     */
    private String scannedByName;
}
