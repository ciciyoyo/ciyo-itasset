package com.ciyocloud.itam.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.itam.enums.StocktakeItemStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


/**
 * 盘点明细对象 itam_stocktake_items
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_stocktake_items")
public class StocktakeItemsEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 关联盘点任务 ID
     */
    @NotNull(message = "关联盘点任务 ID不能为空")
    private Long stocktakeId;
    /**
     * 关联资产 ID
     */
    @NotNull(message = "关联资产 ID不能为空")
    private Long assetId;
    /**
     * 盘点结果
     */
    @NotNull(message = "盘点结果不能为空")
    private StocktakeItemStatus status;
    /**
     * 执行盘点的人员
     */
    @NotNull(message = "执行盘点的人员不能为空")
    private Long scannedBy;
    /**
     * 盘点/扫描时间
     */
    @NotNull(message = "盘点/扫描时间不能为空")
    private LocalDateTime scannedAt;
    /**
     * 系统记录位置 ID
     */
    private Long expectedLocationId;
    /**
     * 实际发现位置 ID
     */
    private Long actualLocationId;
    /**
     * 说明/异常备注
     */
    private String note;


}
