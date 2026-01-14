package com.ciyocloud.itam.req;

import com.ciyocloud.itam.enums.AllocationOwnerType;
import com.ciyocloud.itam.enums.AllocationStatus;
import com.ciyocloud.itam.enums.AssetType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 资源分配分页查询请求
 */
@Data
public class AllocationsPageReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 资源类型
     */
    @NotNull(message = "资产类型不能为空")
    private AssetType itemType;

    /**
     * 资源 ID
     */
    private Long itemId;

    /**
     * 归属者类型
     */
    private AllocationOwnerType ownerType;

    /**
     * 归属者 ID
     */
    private Long ownerId;

    /**
     * 状态
     */
    private AllocationStatus status;

    /**
     * 资源名称
     */
    private String itemName;

    /**
     * 归属者名称
     */
    private String ownerName;

    /**
     * 备注
     */
    private String note;
}
