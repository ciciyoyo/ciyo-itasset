package com.ciyocloud.itam.vo;

import lombok.Data;

/**
 * 供应商服务异常统计 VO
 */
@Data
public class SupplierFailureStatsVO {

    /**
     * 供应商 ID
     */
    private Long supplierId;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 异常数量
     */
    private Long failureCount;
}
