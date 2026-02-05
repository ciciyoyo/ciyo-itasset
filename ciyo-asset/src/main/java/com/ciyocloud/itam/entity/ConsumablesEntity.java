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
 * 耗材对象 itam_consumables
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_consumables")
@ExcelIgnoreUnannotated
public class ConsumablesEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "耗材ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long id;
    /**
     * 耗材名称
     */
    @NotBlank(message = "耗材名称不能为空")
    @ExcelProperty(value = "耗材名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("HP CF400A 黑色墨盒")
    private String name;
    /**
     * 分类 ID
     */
    @NotNull(message = "分类 ID不能为空")
    @ExcelProperty(value = "分类ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long categoryId;
    /**
     * 厂商 ID
     */
    @ExcelProperty(value = "厂商ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long manufacturerId;
    /**
     * 物品编号/型号
     */
    @ExcelProperty(value = "物品编号/型号")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("CF400A")
    private String itemNo;
    /**
     * 采购单号
     */
    @ExcelProperty(value = "采购单号")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("PO-2024-001")
    private String orderNumber;
    /**
     * 购买日期
     */
    @ExcelProperty(value = "购买日期")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("2024-01-15 10:30:00")
    private LocalDateTime purchaseDate;
    /**
     * 购买成本
     */
    @ExcelProperty(value = "购买成本")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("150.50")
    private BigDecimal purchaseCost;
    /**
     * 当前库存数量
     */
    @NotNull(message = "当前库存数量不能为空")
    @ExcelProperty(value = "当前库存数量")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("100")
    private Long quantity;
    /**
     * 最小库存预警
     */
    @ExcelProperty(value = "最小库存预警")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("10")
    private Long minQuantity;
    /**
     * 存放位置 ID
     */
    @ExcelProperty(value = "存放位置ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long locationId;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
