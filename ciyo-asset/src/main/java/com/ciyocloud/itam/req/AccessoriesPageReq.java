package com.ciyocloud.itam.req;

import com.ciyocloud.common.entity.request.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配件分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccessoriesPageReq extends BaseRequest {

    /**
     * 配件名称
     */
    private String name;

    /**
     * 型号 ID
     */
    private Long modelId;

    /**
     * 状态
     */
    private Integer assetsStatus;

    /**
     * 供应商 ID
     */
    private Long supplierId;

    /**
     * 资产标签
     */
    private String assetTag;
}
