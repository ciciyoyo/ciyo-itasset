package com.ciyocloud.itam.vo;

import com.ciyocloud.itam.enums.AssetRequestStatus;
import com.ciyocloud.itam.enums.AssetType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资产申请 VO
 *
 * @author codeck
 * @since 2026-01-10
 */
@Data
public class AssetRequestsVO {

    private Long id;
    private String requestNo;
    private Long userId;
    private String userName; // 申请人姓名
    private AssetType itemType;
    private Long categoryId;
    private String categoryName; // 分类名称
    private Long itemId;
    private String itemName; // 资产名称 (如果指定了具体资产)
    private Integer quantity;
    private String reason;
    private AssetRequestStatus status;
    private Long approverId;
    private String approverName; // 审批人姓名
    private LocalDateTime approvalTime;
    private String approvalNote;
    private LocalDateTime createTime;
}
