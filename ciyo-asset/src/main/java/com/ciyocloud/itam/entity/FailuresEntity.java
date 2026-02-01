package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.excel.convert.DictEnumConvert;
import com.ciyocloud.excel.core.EnumSampleProvider;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.enums.FailureStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 故障表 itam_failures
 *
 * @author codeck
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_failures")
@ExcelIgnoreUnannotated
public class FailuresEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 关联类型 (service, device, accessory, other)
     */
    @NotNull(message = "关联类型不能为空")
    @ExcelProperty(value = "关联类型", converter = DictEnumConvert.class)
    @ExcelSample(provider = EnumSampleProvider.class)
    private AssetType targetType;

    /**
     * 关联目标 ID
     */
    @NotNull(message = "关联目标 ID不能为空")
    @ExcelProperty(value = "关联目标ID")
    private Long targetId;

    /**
     * 故障名称
     */
    @NotBlank(message = "故障名称不能为空")
    @ExcelProperty(value = "故障名称")
    private String failureName;

    /**
     * 故障描述
     */
    @ExcelProperty(value = "故障描述")
    private String failureDescription;

    /**
     * 故障发生时间
     */
    @NotNull(message = "故障发生时间不能为空")
    @ExcelProperty(value = "故障发生时间")
    private LocalDateTime failureDate;

    /**
     * 故障状态
     */
    @NotNull(message = "故障状态不能为空")
    @ExcelProperty(value = "故障状态", converter = DictEnumConvert.class)
    @ExcelSample(provider = EnumSampleProvider.class)
    private FailureStatus status;

    /**
     * 报告人 ID
     */
    @ExcelProperty(value = "报告人ID")
    private Long reportedBy;

    /**
     * 修复人 ID
     */
    @ExcelProperty(value = "修复人ID")
    private Long resolvedBy;

    /**
     * 修复日期
     */
    @ExcelProperty(value = "修复日期")
    private LocalDateTime resolvedDate;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String notes;


}
