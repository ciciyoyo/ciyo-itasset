package com.ciyocloud.itam.req;

import com.ciyocloud.common.entity.request.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 设备管理分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DevicePageReq extends BaseRequest {
    private static final long serialVersionUID = 1L;

    /**
     * 资产标签
     */
    private String assetTag;

    /**
     * 设备编号
     */
    private String deviceNo;

    /**
     * 关联型号 ID
     */
    private Long modelId;

    /**
     * 状态
     */
    private Integer assetsStatus;

    /**
     * 序列号
     */
    private String serial;

    /**
     * 资产名称
     */
    private String name;

    /**
     * 购买日期
     */
    private LocalDate purchaseDate;

    /**
     * 分配给谁 (User ID)
     */
    private Long assignedTo;

    /**
     * 分配给谁名称
     */
    private String assignedToName;

    /**
     * 物理位置 ID
     */
    private Long locationId;

    /**
     * 供应商 ID
     */
    private Long supplierId;

    /**
     * 关联折旧规则 ID
     */
    private Long depreciationId;
}
