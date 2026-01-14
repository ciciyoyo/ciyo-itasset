package com.ciyocloud.itam.vo;

import com.ciyocloud.itam.entity.FailuresEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 故障VO
 *
 * @author codeck
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FailuresVO extends FailuresEntity {

    /**
     * 关联目标名称
     */
    private String targetName;

    /**
     * 报告人名称
     */
    private String reportedByName;

    /**
     * 解决人名称
     */
    private String resolvedByName;
}
