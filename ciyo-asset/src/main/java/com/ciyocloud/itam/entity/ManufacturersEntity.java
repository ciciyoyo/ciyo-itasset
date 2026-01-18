package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 制造商对象 itam_manufacturers
 *
 * @author codeck
 * @since 2025-12-29 15:58:20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_manufacturers")
@ExcelIgnoreUnannotated
public class ManufacturersEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "厂商ID")
    private Long id;
    /**
     * 厂商名称
     */
    @NotBlank(message = "厂商名称不能为空")
    @ExcelProperty(value = "厂商名称")
    private String name;
    /**
     * 官网 URL
     */
    @ExcelProperty(value = "官网URL")
    private String officialUrl;
    /**
     * 网站客服支持
     */
    @ExcelProperty(value = "网站客服支持")
    private String supportUrl;
    /**
     * 保修查询 URL
     */
    @ExcelProperty(value = "保修查询URL")
    private String warrantyUrl;
    /**
     * 电话客服支持
     */
    @ExcelProperty(value = "电话客服支持")
    private String supportPhone;
    /**
     * 邮件客服支持
     */
    @ExcelProperty(value = "邮件客服支持")
    private String supportEmail;
    /**
     * 厂商 Logo
     */
    @ExcelProperty(value = "厂商Logo")
    private String logoUrl;
    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;


    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
