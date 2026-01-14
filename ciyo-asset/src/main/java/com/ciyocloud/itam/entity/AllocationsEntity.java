package com.ciyocloud.itam.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.itam.enums.AllocationOwnerType;
import com.ciyocloud.itam.enums.AllocationStatus;
import com.ciyocloud.itam.enums.AssetType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 资源分配/领用明细表 itam_allocations
 *
 * @author codeck
 * @since 2025-12-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_allocations")
public class AllocationsEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 资源类型 (device, license, accessory)
     */
    @NotNull(message = "资源类型不能为空")
    private AssetType itemType;

    /**
     * 资源 ID (对应 itam_device.id, itam_licenses.id 等)
     */
    @NotNull(message = "资源 ID不能为空")
    private Long itemId;

    /**
     * 归属者类型 (user:员工, location:地点/部门, asset:设备)
     */
    @NotNull(message = "归属者类型不能为空")
    private AllocationOwnerType ownerType;

    /**
     * 归属者 ID
     */
    @NotNull(message = "归属者 ID不能为空")
    private Long ownerId;

    /**
     * 数量 (资产固定为 1，软件/配件可 > 1)
     */
    @NotNull(message = "数量不能为空")
    private Integer quantity;

    /**
     * 状态 (reserved:预约/规划, active:占用中, returned:已归还)
     */
    private AllocationStatus status;

    /**
     * 分配/预计领用时间
     */
    @NotNull(message = "分配/领用时间不能为空")
    private LocalDateTime assignDate;

    /**
     * 预计归还时间
     */
    private LocalDateTime expectedReturnDate;

    /**
     * 实际归还时间 (当状态变更为 returned 时填入)
     */
    private LocalDateTime returnDate;

    /**
     * 备注
     */
    private String note;


    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
