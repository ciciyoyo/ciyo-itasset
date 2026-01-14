package com.ciyocloud.itam.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 软件授权分页查询请求
 *
 * @author codeck
 * @since 2025-12-31
 */
@Data
public class LicensePageReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 软件名称
     */
    private String name;

    /**
     * 序列号/密钥
     */
    private String licenseKey;

    /**
     * 厂商 ID
     */
    private Long manufacturerId;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 供应商 ID
     */
    private Long supplierId;

    /**
     * 订单号
     */
    private String orderNumber;
}
