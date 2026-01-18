package com.ciyocloud.itam.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ciyocloud.itam.entity.DeviceEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备管理VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ExcelIgnoreUnannotated
public class DeviceVO extends DeviceEntity {
    /**
     * 型号名称
     */
    @ExcelProperty(value = "型号名称")
    private String modelName;

    /**
     * 供应商名称
     */
    @ExcelProperty(value = "供应商名称")
    private String supplierName;

    /**
     * 存放位置名称
     */
    @ExcelProperty(value = "存放位置名称")
    private String locationName;

    /**
     * 折旧规则名称
     */
    @ExcelProperty(value = "折旧规则名称")
    private String depreciationName;

    /**
     * 分配给谁名称
     */
    @ExcelProperty(value = "分配给谁名称")
    private String assignedToName;
}

