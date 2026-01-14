package com.ciyocloud.itam.req;

import com.ciyocloud.itam.enums.AllocationOwnerType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 软件授权分配请求
 *
 * @author codeck
 * @since 2025-12-31
 */
@Data
public class LicenseAllocationReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 软件授权 ID
     */
    @NotNull(message = "授权id不能为空")
    private Long licenseId;

    /**
     * 归属类型 (user:员工, asset:设备)
     */
    @NotNull(message = "ownerType不能为空")
    private AllocationOwnerType ownerType;

    /**
     * 归属 ID (员工ID 或 设备ID)
     */
    @NotNull
    private Long ownerId;

    /**
     * 数量 (默认 1)
     */
    private Integer quantity;

    /**
     * 备注
     */
    private String note;
}
