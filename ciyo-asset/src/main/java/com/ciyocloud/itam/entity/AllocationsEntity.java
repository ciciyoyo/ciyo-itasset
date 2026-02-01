package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.excel.convert.DictEnumConvert;
import com.ciyocloud.excel.core.EnumSampleProvider;
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
@ExcelIgnoreUnannotated
public class AllocationsEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "分配ID")
    private Long id;

    /**
     * 资源类型 (device, license, accessory)
     */
    @NotNull(message = "资源类型不能为空")
    @ExcelProperty(value = "资源类型", converter = DictEnumConvert.class)
    @ExcelSample(provider = EnumSampleProvider.class)
    private AssetType itemType;

    /**
     * 资源 ID (对应 itam_device.id, itam_licenses.id 等)
     */
    @NotNull(message = "资源 ID不能为空")
    @ExcelProperty(value = "资源ID")
    private Long itemId;

    /**
     * 归属者类型 (user:员工, location:地点/部门, asset:设备)
     */
    @NotNull(message = "归属者类型不能为空")
    @ExcelProperty(value = "归属者类型", converter = DictEnumConvert.class)
    @ExcelSample(provider = EnumSampleProvider.class)
    private AllocationOwnerType ownerType;

    /**
     * 归属者 ID
     */
    @NotNull(message = "归属者 ID不能为空")
    @ExcelProperty(value = "归属者ID")
    private Long ownerId;

    /**
     * 数量 (资产固定为 1，软件/配件可 > 1)
     */
    @NotNull(message = "数量不能为空")
    @ExcelProperty(value = "数量")
    private Integer quantity;

    /**
     * 状态 (reserved:预约/规划, active:占用中, returned:已归还)
     */
    @ExcelProperty(value = "状态", converter = DictEnumConvert.class)
    @ExcelSample(provider = EnumSampleProvider.class)
    private AllocationStatus status;

    /**
     * 分配/预计领用时间
     */
    @NotNull(message = "分配/领用时间不能为空")
    @ExcelProperty(value = "分配/领用时间")
    private LocalDateTime assignDate;

    /**
     * 预计归还时间
     */
    @ExcelProperty(value = "预计归还时间")
    private LocalDateTime expectedReturnDate;

    /**
     * 实际归还时间 (当状态变更为 returned 时填入)
     */
    @ExcelProperty(value = "实际归还时间")
    private LocalDateTime returnDate;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String note;


    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
