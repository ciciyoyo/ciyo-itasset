package com.ciyocloud.itam.req;

import com.ciyocloud.itam.enums.StocktakeStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 盘点任务分页查询请求
 */
@Data
public class StocktakesPageReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 盘点任务名称
     */
    private String name;

    /**
     * 盘点位置范围 ID
     */
    private Long locationId;

    /**
     * 盘点分类范围 ID
     */
    private Long categoryId;

    /**
     * 状态
     */
    private StocktakeStatus status;

    /**
     * 负责人 ID
     */
    private Long managerId;

    /**
     * 计划开始日期
     */
    private LocalDateTime startDate;

    /**
     * 计划/实际结束日期
     */
    private LocalDateTime endDate;
}
