package com.ciyocloud.itam.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.itam.enums.AssetRequestStatus;
import com.ciyocloud.itam.enums.AssetType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 资产申请表 itam_asset_requests
 *
 * @author codeck
 * @since 2026-01-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_asset_requests")
public class AssetRequestsEntity extends SysBaseEntity {


    /**
     * 主键 ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 申请单号
     */
    private String requestNo;

    /**
     * 申请人 ID
     */
    @NotNull(message = "申请人不能为空")
    private Long userId;

    /**
     * 资产类型 (device, license, accessory)
     */
    @NotNull(message = "资产类型不能为空")
    private AssetType itemType;

    /**
     * 申请分类 ID (可选, 譬如申请 "笔记本电脑" 类别)
     */
    private Long categoryId;

    /**
     * 具体资产 ID (可选, 如果申请特定资产)
     */
    private Long itemId;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    private Integer quantity;

    /**
     * 申请原因
     */
    private String reason;

    /**
     * 是否长期使用
     */
    private Boolean isLongTerm;

    /**
     * 预计归还时间
     */
    private LocalDateTime expectedReturnTime;

    /**
     * 状态 (pending, approved, rejected, canceled)
     */
    private AssetRequestStatus status;

    /**
     * 审批人 ID
     */
    private Long approverId;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    private String approvalNote;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
