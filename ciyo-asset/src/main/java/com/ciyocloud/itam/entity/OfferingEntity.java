package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.excel.annotation.ExcelTemplate;
import com.ciyocloud.excel.convert.DictEnumConvert;
import com.ciyocloud.excel.core.EnumSampleProvider;
import com.ciyocloud.itam.enums.OfferingStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务对象 itam_offering
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_offering")
@ExcelIgnoreUnannotated
@ExcelTemplate(code = "offering", sheetName = "服务导入模板")
public class OfferingEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "服务ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long id;
    /**
     * 服务名称
     */
    @NotBlank(message = "服务名称不能为空")
    @ExcelProperty(value = "服务名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("云服务器维护")
    private String name;
    /**
     * 服务商/供应商 ID
     */
    @ExcelProperty(value = "服务商/供应商ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long supplierId;
    /**
     * 服务单号/合同号
     */
    @ExcelProperty(value = "服务单号/合同号")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("SRV-2026-001")
    private String serviceNumber;
    /**
     * 开始日期
     */
    @ExcelProperty(value = "开始日期")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("2026-01-01")
    private LocalDateTime startDate;
    /**
     * 结束日期
     */
    @ExcelProperty(value = "结束日期")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("2026-12-31")
    private LocalDateTime endDate;
    /**
     * 费用
     */
    @ExcelProperty(value = "费用")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("10000.00")
    private BigDecimal cost;
    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("年度维护服务")
    private String notes;

    /**
     * 服务状态
     */
    @ExcelProperty(value = "服务状态", converter = DictEnumConvert.class)
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample(provider = EnumSampleProvider.class)
    private OfferingStatus offeringStatus;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
