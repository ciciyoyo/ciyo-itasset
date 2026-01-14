package com.ciyocloud.itam.req;

import com.ciyocloud.itam.enums.AssetRequestStatus;
import com.ciyocloud.itam.enums.AssetType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资产申请分页查询参数
 *
 * @author codeck
 * @since 2026-01-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AssetRequestsPageReq {

    /**
     * 申请人 ID
     */
    private Long userId;

    /**
     * 资产类型
     */
    private AssetType itemType;

    /**
     * 状态
     */
    private AssetRequestStatus status;

    /**
     * 关键字 (单号/申请人)
     */
    private String keyword;
}
