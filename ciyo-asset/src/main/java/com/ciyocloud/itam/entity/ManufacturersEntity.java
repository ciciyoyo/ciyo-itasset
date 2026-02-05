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
 * 制造商对象 itam_manufacturers
 *
 * @author codeck
 * @since 2025-12-29 15:58:20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_manufacturers")
@ExcelIgnoreUnannotated
@ExcelTemplate(code = "manufacturer", sheetName = "厂商导入模板")
public class ManufacturersEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "厂商ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long id;
    /**
     * 厂商名称
     */
    @NotBlank(message = "厂商名称不能为空")
    @ExcelProperty(value = "厂商名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("示例科技有限公司")
    private String name;
    /**
     * 官网 URL
     */
    @ExcelProperty(value = "官网URL")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("https://www.example.com")
    private String officialUrl;
    /**
     * 网站客服支持
     */
    @ExcelProperty(value = "网站客服支持")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("https://support.example.com")
    private String supportUrl;
    /**
     * 保修查询 URL
     */
    @ExcelProperty(value = "保修查询URL")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("https://warranty.example.com")
    private String warrantyUrl;
    /**
     * 电话客服支持
     */
    @ExcelProperty(value = "电话客服支持")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("400-800-1234")
    private String supportPhone;
    /**
     * 邮件客服支持
     */
    @ExcelProperty(value = "邮件客服支持")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("service@example.com")
    private String supportEmail;
    /**
     * 厂商 Logo
     */
    @ExcelProperty(value = "厂商Logo")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("https://cdn.example.com/logo.png")
    private String logoUrl;
    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("优先级A，响应及时")
    private String remark;


    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
