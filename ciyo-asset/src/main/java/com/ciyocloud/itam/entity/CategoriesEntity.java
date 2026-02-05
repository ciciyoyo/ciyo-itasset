package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.excel.annotation.ExcelTemplate;
import com.ciyocloud.itam.enums.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


/**
 * 分类对象 itam_categories
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_categories")
@ExcelIgnoreUnannotated
@ExcelTemplate(code = "category", sheetName = "分类导入模板")
public class CategoriesEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "分类ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long id;
    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @ExcelProperty(value = "分类名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("台式机")
    private String name;
    /**
     * 分类编码
     */
    @NotBlank(message = "分类编码不能为空")
    @ExcelProperty(value = "分类编码")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("DESKTOP_COMPUTER")
    private String code;
    /**
     * 父级 ID
     */
    @ExcelProperty(value = "父级ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long parentId;

    /**
     * 父级分类名称（导入用）
     */
    @TableField(exist = false)
    @ExcelProperty(value = "父级分类名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("IT设备")
    private String parentName;

    /**
     * 分类大类
     */
    @NotNull(message = "分类大类不能为空")
    private AssetType categoryType;

    /**
     * 子菜单 用于树结构
     */
    @TableField(exist = false)
    private List<CategoriesEntity> children;


    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
