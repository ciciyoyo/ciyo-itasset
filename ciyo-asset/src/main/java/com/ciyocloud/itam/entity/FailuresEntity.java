package com.ciyocloud.itam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
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
public class FailuresEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 关联类型 (service, device, accessory, other)
     */
    @NotNull(message = "关联类型不能为空")
    private AssetType targetType;

    /**
     * 关联目标 ID
     */
    @NotNull(message = "关联目标 ID不能为空")
    private Long targetId;

    /**
     * 故障名称
     */
    @NotBlank(message = "故障名称不能为空")
    private String failureName;

    /**
     * 故障描述
     */
    private String failureDescription;

    /**
     * 故障发生时间
     */
    @NotNull(message = "故障发生时间不能为空")
    private LocalDateTime failureDate;

    /**
     * 故障状态
     */
    @NotNull(message = "故障状态不能为空")
    private FailureStatus status;

    /**
     * 报告人 ID
     */
    private Long reportedBy;

    /**
     * 修复人 ID
     */
    private Long resolvedBy;

    /**
     * 修复日期
     */
    private LocalDateTime resolvedDate;

    /**
     * 备注
     */
    private String notes;


}
