package com.ciyocloud.itam.req;

import com.ciyocloud.itam.enums.StocktakeItemStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 盘点明细分页查询请求
 */
@Data
public class StocktakeItemsPageReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 关联盘点任务 ID
     */
    private Long stocktakeId;

    /**
     * 盘点任务名称
     */
    private String stocktakeName;

    /**
     * 关联资产 ID
     */
    private Long assetId;

    /**
     * 资产名称
     */
    private String assetName;

    /**
     * 盘点结果
     */
    private StocktakeItemStatus status;

    /**
     * 执行盘点的人员
     */
    private Long scannedBy;

    /**
     * 盘点/扫描时间
     */
    private LocalDateTime scannedAt;

    /**
     * 系统记录位置 ID
     */
    private Long expectedLocationId;

    /**
     * 实际发现位置 ID
     */
    private Long actualLocationId;
}
