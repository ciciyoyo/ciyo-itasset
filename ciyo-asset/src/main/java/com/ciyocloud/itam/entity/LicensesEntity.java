package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 软件授权对象 itam_licenses
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_licenses")
@ExcelIgnoreUnannotated
public class LicensesEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "软件授权ID")
    private Long id;
    /**
     * 软件名称
     */
    @NotBlank(message = "软件名称不能为空")
    @ExcelProperty(value = "软件名称")
    private String name;
    /**
     * 序列号/密钥
     */
    @NotBlank(message = "序列号/密钥不能为空")
    @ExcelProperty(value = "序列号/密钥")
    private String licenseKey;
    /**
     * 总授权数
     */
    @NotNull(message = "总授权数不能为空")
    @ExcelProperty(value = "总授权数")
    private Long totalSeats;
    /**
     * 厂商 ID
     */
    @NotNull(message = "厂商 ID不能为空")
    @ExcelProperty(value = "厂商ID")
    private Long manufacturerId;
    /**
     * 分类 ID
     */
    @ExcelProperty(value = "分类ID")
    private Long categoryId;
    /**
     * 最少数量
     */
    @ExcelProperty(value = "最少数量")
    private Long minQty;

    /**
     * 许可人名字
     */
    @ExcelProperty(value = "许可人名字")
    private String licensedToName;
    /**
     * 许可人电子邮件
     */
    @ExcelProperty(value = "许可人电子邮件")
    private String licensedToEmail;

    /**
     * 供应商 ID
     */
    @ExcelProperty(value = "供应商ID")
    private Long supplierId;
    /**
     * 订单号
     */
    @ExcelProperty(value = "订单号")
    private String orderNumber;
    /**
     * 采购价格
     */
    @ExcelProperty(value = "采购价格")
    private BigDecimal purchaseCost;
    /**
     * 购买日期
     */
    @ExcelProperty(value = "购买日期")
    private LocalDateTime purchaseDate;
    /**
     * 到期日期
     */
    @ExcelProperty(value = "到期日期")
    private LocalDateTime expirationDate;
    /**
     * 终止日期
     */
    @ExcelProperty(value = "终止日期")
    private LocalDateTime terminationDate;
    /**
     * 采购订单号码
     */
    @ExcelProperty(value = "采购订单号码")
    private String purchaseOrderNumber;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String notes;
    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
