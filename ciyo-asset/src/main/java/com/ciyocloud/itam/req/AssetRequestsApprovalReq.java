package com.ciyocloud.itam.req;

import com.ciyocloud.itam.enums.AssetRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 资产申请审批参数
 *
 * @author codeck
 * @since 2026-01-10
 */
@Data
public class AssetRequestsApprovalReq {

    /**
     * 申请 ID
     */
    @NotNull(message = "申请 ID不能为空")
    private Long id;

    /**
     * 审批结果 (approved, rejected)
     */
    @NotNull(message = "审批结果不能为空")
    private AssetRequestStatus status;

    /**
     * 分配的资产 ID (如果是设备申请，审批时可能需要指定具体分配哪个设备)
     */
    private Long allocatedItemId;

    /**
     * 审批意见
     */
    private String approvalNote;
}
