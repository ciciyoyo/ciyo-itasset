package com.ciyocloud.itam.vo;

import com.ciyocloud.itam.entity.DeviceEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备管理VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceVO extends DeviceEntity {
    /**
     * 型号名称
     */
    private String modelName;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 存放位置名称
     */
    private String locationName;

    /**
     * 折旧规则名称
     */
    private String depreciationName;

    /**
     * 分配给谁名称
     */
    private String assignedToName;
}
