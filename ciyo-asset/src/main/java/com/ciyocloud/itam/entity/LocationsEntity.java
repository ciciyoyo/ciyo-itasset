package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.excel.annotation.ExcelTemplate;
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
@ExcelTemplate(code = "location", sheetName = "物理位置导入模板")
public class LocationsEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "位置ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long id;
    /**
     * 位置名称
     */
    @NotBlank(message = "位置名称不能为空")
    @ExcelProperty(value = "位置名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("北京分公司")
    private String name;
    /**
     * 父级 ID
     */
    @ExcelProperty(value = "父级ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long parentId;
    
    /**
     * 父级位置名称（导入用）
     */
    @TableField(exist = false)
    @ExcelProperty(value = "父级位置名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("北京总部")
    private String parentName;
    
    /**
     * 该处负责人
     */
    @ExcelProperty(value = "负责人ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
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
