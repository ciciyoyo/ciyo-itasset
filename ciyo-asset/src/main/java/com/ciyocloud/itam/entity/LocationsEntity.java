package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 物理位置对象 itam_locations
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_locations")
@ExcelIgnoreUnannotated
public class LocationsEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "位置ID")
    private Long id;
    /**
     * 位置名称
     */
    @NotBlank(message = "位置名称不能为空")
    @ExcelProperty(value = "位置名称")
    private String name;
    /**
     * 父级 ID
     */
    @ExcelProperty(value = "父级ID")
    private Long parentId;
    /**
     * 该处负责人
     */
    @ExcelProperty(value = "负责人ID")
    private Long managerId;

    /**
     * 子节点
     */
    @TableField(exist = false)
    private java.util.List<LocationsEntity> children;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
