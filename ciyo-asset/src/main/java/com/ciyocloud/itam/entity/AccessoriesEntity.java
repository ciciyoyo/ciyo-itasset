package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.annotation.ExcelSample;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配件对象 itam_accessories
 *
 * @author codeck
 * @since 2025-12-29 20:10:26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_accessories")
@ExcelIgnoreUnannotated
public class AccessoriesEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "配件ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long id;
    /**
     * 配件名称
     */
    @NotBlank(message = "配件名称不能为空")
    @ExcelProperty(value = "配件名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("金士顿 DDR4 8GB 内存条")
    private String name;
    /**
     * 资产编号
     */
    @ExcelProperty(value = "资产编号")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("ACC-2024-001")
    private String assetNumber;
    /**
     * 分类 ID
     */
    @NotNull(message = "分类 ID不能为空")
    @ExcelProperty(value = "分类ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long categoryId;


    /**
     * 供应商 ID
     */
    @ExcelProperty(value = "供应商ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long supplierId;


    /**
     * 厂商 ID
     */
    @ExcelProperty(value = "厂商ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long manufacturerId;


    /**
     * 规格
     */
    @ExcelProperty(value = "规格")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("DDR4-3200 8GB")
    private String specifications;
    /**
     * 存放位置 ID
     */
    @ExcelProperty(value = "存放位置ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long locationId;


    /**
     * 关联折旧规则 ID
     */
    @ExcelProperty(value = "折旧规则ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long depreciationId;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @ExcelProperty(value = "数量")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("5")
    private Long quantity;
    /**
     * 最小库存预警
     */
    @ExcelProperty(value = "最小库存预警")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("2")
    private Long minQuantity;
    /**
     * 购买日期
     */
    @ExcelProperty(value = "购买日期")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("2024-01-15 10:30:00")
    private LocalDateTime purchaseDate;
    /**
     * 过保日期
     */
    @ExcelProperty(value = "过保日期")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("2027-01-15 00:00:00")
    private LocalDateTime warrantyExpirationDate;
    /**
     * 购买成本
     */
    @ExcelProperty(value = "购买成本")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("299.99")
    private BigDecimal purchaseCost;
    /**
     * 订单号
     */
    @ExcelProperty(value = "订单号")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("PO-2024-001")
    private String orderNumber;
    /**
     * 配件序列号
     */
    @ExcelProperty(value = "配件序列号")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("KVR32N22S8-8")
    private String serialNumber;
    /**
     * 描述
     */
    @ExcelProperty(value = "描述")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("用于服务器内存扩展")
    private String description;
    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
