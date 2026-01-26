package com.ciyocloud.itam.vo;

import com.ciyocloud.itam.enums.StocktakeItemStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 设备扫码详情 VO，包含盘点信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceScanVO extends DeviceDetailVO {

    /**
     * 上次盘点时间
     */
    private LocalDateTime lastAuditDate;

    /**
     * 上次盘点结果
     */
    private StocktakeItemStatus lastAuditStatus;

    /**
     * 上次盘点说明
     */
    private String lastAuditNote;
}
