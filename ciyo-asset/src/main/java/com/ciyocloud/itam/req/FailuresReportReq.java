package com.ciyocloud.itam.req;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 故障报告请求
 */
@Data
public class FailuresReportReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 关联目标 ID
     */
    private Long targetId;

    /**
     * 故障名称
     */
    private String failureName;

    /**
     * 故障描述
     */
    private String failureDescription;

    /**
     * 故障发生时间
     */
    private LocalDateTime failureDate;

    /**
     * 备注
     */
    private String notes;
}
