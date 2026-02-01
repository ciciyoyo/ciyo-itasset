package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.excel.annotation.ExcelTemplate;
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
@ExcelTemplate(code = "license", sheetName = "软件授权导入模板")
public class LicensesEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "软件授权ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long id;
    /**
     * 软件名称
     */
    @NotBlank(message = "软件名称不能为空")
    @ExcelProperty(value = "软件名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("Microsoft Office 365")
    private String name;
    /**
     * 序列号/密钥
     */
    @NotBlank(message = "序列号/密钥不能为空")
    @ExcelProperty(value = "序列号/密钥")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("XXXXX-XXXXX-XXXXX-XXXXX")
    private String licenseKey;
    /**
     * 总授权数
     */
    @NotNull(message = "总授权数不能为空")
    @ExcelProperty(value = "总授权数")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("50")
    private Long totalSeats;
    /**
     * 厂商 ID
     */
    @NotNull(message = "厂商 ID不能为空")
    @ExcelProperty(value = "厂商ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long manufacturerId;
    /**
     * 分类 ID
     */
    @ExcelProperty(value = "分类ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long categoryId;
    /**
     * 最少数量
     */
    @ExcelProperty(value = "最少数量")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("5")
    private Long minQty;

    /**
     * 许可人名字
     */
    @ExcelProperty(value = "许可人名字")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("张三")
    private String licensedToName;
    /**
     * 许可人电子邮件
     */
    @ExcelProperty(value = "许可人电子邮件")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("zhangsan@example.com")
    private String licensedToEmail;

    /**
     * 供应商 ID
     */
    @ExcelProperty(value = "供应商ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long supplierId;
    /**
     * 订单号
     */
    @ExcelProperty(value = "订单号")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("PO-2026-001")
    private String orderNumber;
    /**
     * 采购价格
     */
    @ExcelProperty(value = "采购价格")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("5000.00")
    private BigDecimal purchaseCost;
    /**
     * 购买日期
     */
    @ExcelProperty(value = "购买日期")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("2026-01-15 10:00:00")
    private LocalDateTime purchaseDate;
    /**
     * 到期日期
     */
    @ExcelProperty(value = "到期日期")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("2027-01-15 23:59:59")
    private LocalDateTime expirationDate;
    /**
     * 终止日期
     */
    @ExcelProperty(value = "终止日期")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("2027-12-31 23:59:59")
    private LocalDateTime terminationDate;
    /**
     * 采购订单号码
     */
    @ExcelProperty(value = "采购订单号码")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("PO-2026-001")
    private String purchaseOrderNumber;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("企业版授权")
    private String notes;
    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
