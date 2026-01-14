package com.ciyocloud.itam.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 个人首页统计数据
 */
@Data
public class PersonalStatsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 我的设备数量
     */
    private Long deviceCount;

    /**
     * 待处理申请数量
     */
    private Long pendingRequestCount;

    /**
     * 使用天数 (自某日期起算)
     */
    private Long daysInUse;

    /**
     * 即将到期数量
     */
    private Long expiringCount;

    /**
     * 我的设备描述 (e.g. "MacBook Pro, iPhone 15")
     */
    private String deviceDetail;

    /**
     * 待处理申请描述 (e.g. "设备申请 - 2026/01/09")
     */
    private String pendingRequestDetail;

    /**
     * 使用天数描述 (e.g. "自 2025-08-05 起算")
     */
    private String daysInUseDetail;

    /**
     * 即将到期描述 (e.g. "暂无设备即将到期")
     */
    private String expiringDetail;
}
