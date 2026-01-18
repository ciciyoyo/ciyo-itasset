package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.excel.convert.DictEnumConvert;
import com.ciyocloud.itam.enums.DeviceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备管理对象 itam_device
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_device")
@ExcelIgnoreUnannotated
public class DeviceEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "设备ID")
    private Long id;
    /**
     * 关联型号 ID
     */
    @NotNull(message = "关联型号 ID不能为空")
    @ExcelProperty(value = "关联型号ID")
    private Long modelId;
    /**
     * 设备状态
     */
    @NotNull(message = "设备状态不能为空")
    @ExcelProperty(value = "设备状态", converter = DictEnumConvert.class)
    private DeviceStatus assetsStatus;
    /**
     * 序列号
     */
    @ExcelProperty(value = "序列号")
    private String serial;
    /**
     * 设备编号
     */
    @ExcelProperty(value = "设备编号")
    private String deviceNo;
    /**
     * 设备名称
     */
    @ExcelProperty(value = "设备名称")
    private String name;
    /**
     * 设备图片
     */
    @ExcelProperty(value = "设备图片")
    private String imageUrl;
    /**
     * 购买日期
     */
    @ExcelProperty(value = "购买日期")
    private LocalDate purchaseDate;
    /**
     * 购买成本
     */
    @ExcelProperty(value = "购买成本")
    private BigDecimal purchaseCost;
    /**
     * 过保日期
     */
    @ExcelProperty(value = "过保日期")
    private LocalDate warrantyDate;
    /**
     * 描述
     */
    @ExcelProperty(value = "描述")
    private String description;
    /**
     * 分配给谁 (User ID)
     */
    @ExcelProperty(value = "分配给谁ID")
    private Long assignedTo;
    /**
     * 物理位置 ID
     */
    @ExcelProperty(value = "物理位置ID")
    private Long locationId;
    /**
     * 供应商 ID
     */
    @ExcelProperty(value = "供应商ID")
    private Long supplierId;
    /**
     * 关联折旧规则 ID
     */
    @ExcelProperty(value = "折旧规则ID")
    private Long depreciationId;
    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
