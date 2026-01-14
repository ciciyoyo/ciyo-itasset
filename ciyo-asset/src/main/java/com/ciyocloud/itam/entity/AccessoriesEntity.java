package com.ciyocloud.itam.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
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
public class AccessoriesEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 配件名称
     */
    @NotBlank(message = "配件名称不能为空")
    private String name;
    /**
     * 资产编号
     */
    private String assetNumber;
    /**
     * 分类 ID
     */
    @NotNull(message = "分类 ID不能为空")
    private Long categoryId;
    /**
     * 供应商 ID
     */
    private Long supplierId;
    /**
     * 厂商 ID
     */
    private Long manufacturerId;
    /**
     * 规格
     */
    private String specifications;
    /**
     * 存放位置 ID
     */
    private Long locationId;
    /**
     * 关联折旧规则 ID
     */
    private Long depreciationId;
    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    private Long quantity;
    /**
     * 最小库存预警
     */
    private Long minQuantity;
    /**
     * 购买日期
     */
    private LocalDateTime purchaseDate;
    /**
     * 过保日期
     */
    private LocalDateTime warrantyExpirationDate;
    /**
     * 购买成本
     */
    private BigDecimal purchaseCost;
    /**
     * 订单号
     */
    private String orderNumber;
    /**
     * 配件序列号
     */
    private String serialNumber;
    /**
     * 描述
     */
    private String description;
    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
