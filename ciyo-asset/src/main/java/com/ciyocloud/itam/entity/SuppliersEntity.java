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
 * 供应商对象 itam_suppliers
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_suppliers")
@ExcelIgnoreUnannotated
@ExcelTemplate(code = "supplier", sheetName = "供应商导入模板")
public class SuppliersEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "供应商ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long id;
    /**
     * 供应商名称
     */
    @NotBlank(message = "供应商名称不能为空")
    @ExcelProperty(value = "供应商名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("XX科技有限公司")
    private String name;
    /**
     * 联系人
     */
    @ExcelProperty(value = "联系人")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("张三")
    private String contactName;
    /**
     * 联系电话
     */
    @ExcelProperty(value = "联系电话")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("13800138000")
    private String phone;
    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("优质供应商")
    private String remark;


    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
