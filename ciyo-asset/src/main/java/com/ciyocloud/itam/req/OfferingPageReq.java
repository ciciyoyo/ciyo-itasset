package com.ciyocloud.itam.req;

import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.enums.OfferingStatus;
import lombok.Data;

import java.io.Serializable;

/**
 * 服务列表查询请求
 *
 * @author codeck
 * @since 2025-12-31
 */
@Data
public class OfferingPageReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 服务名称
     */
    private String name;

    /**
     * 服务单号
     */
    private String serviceNumber;

    /**
     * 服务状态
     */
    private OfferingStatus offeringStatus;

    /**
     * 关联类型
     */
    private AssetType targetType;

    /**
     * 关联目标 ID
     */
    private Long targetId;

    /**
     * 供应商 ID
     */
    private Long supplierId;

    /**
     * 快捷查询类型: 1-分配到设备的服务, 2-异常的服务
     */
    private Integer quickSearchType;
}
