package com.ciyocloud.itam.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
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
public class DeviceEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 关联型号 ID
     */
    @NotNull(message = "关联型号 ID不能为空")
    private Long modelId;
    /**
     * 设备状态
     */
    @NotNull(message = "设备状态不能为空")
    private DeviceStatus assetsStatus;
    /**
     * 序列号
     */
    private String serial;
    /**
     * 设备编号
     */
    private String deviceNo;
    /**
     * 设备名称
     */
    private String name;
    /**
     * 设备图片
     */
    private String imageUrl;
    /**
     * 购买日期
     */
    private LocalDate purchaseDate;
    /**
     * 购买成本
     */
    private BigDecimal purchaseCost;
    /**
     * 过保日期
     */
    private LocalDate warrantyDate;
    /**
     * 描述
     */
    private String description;
    /**
     * 分配给谁 (User ID)
     */
    private Long assignedTo;
    /**
     * 物理位置 ID
     */
    private Long locationId;
    /**
     * 供应商 ID
     */
    private Long supplierId;
    /**
     * 关联折旧规则 ID
     */
    private Long depreciationId;
    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
