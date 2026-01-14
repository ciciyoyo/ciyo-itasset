package com.ciyocloud.itam.req;

import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.enums.FailureStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 故障分页查询请求对象
 *
 * @author codeck
 * @since 2026-01-01
 */
@Data
public class FailuresPageReq {

    /**
     * 关联目标名称
     */
    private Long targetId;

    /**
     * 关联类型
     */
    @NotNull(message = "关联类型不能为空")
    private AssetType targetType;

    /**
     * 故障名称
     */
    private String failureName;

    /**
     * 故障状态
     */
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
     * 开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

}
