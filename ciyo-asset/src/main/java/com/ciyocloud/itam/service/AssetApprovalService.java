package com.ciyocloud.itam.service;

import com.ciyocloud.itam.entity.AssetRequestsEntity;

/**
 * 资产申请审批处理服务
 * 处理审批通过后的资产分配、库存扣减等逻辑
 *
 * @author codeck
 * @since 2026-01-16
 */
public interface AssetApprovalService {

    /**
     * 处理审批通过后的逻辑
     *
     * @param request          申请单实体
     * @param allocatedItemId  分配的资产ID（设备ID、耗材ID等）
     */
    void handleApproval(AssetRequestsEntity request, Long allocatedItemId);
}
