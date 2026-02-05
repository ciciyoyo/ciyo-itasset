package com.ciyocloud.itam.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.excel.annotation.ExcelTemplate;
import com.ciyocloud.itam.entity.AccessoriesEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配件VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ExcelIgnoreUnannotated
@ExcelTemplate(code = "accessories", sheetName = "配件导入模板")
public class AccessoriesVO extends AccessoriesEntity {
    /**
     * 分类名称
     */
    @ExcelProperty(value = "分类名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("内存条")
    private String categoryName;
    /**
     * 供应商名称
     */
    @ExcelProperty(value = "供应商名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("XX科技")
    private String supplierName;
    /**
     * 厂商名称
     */
    @ExcelProperty(value = "厂商名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("金士顿")
    private String manufacturerName;
    /**
     * 存放位置名称
     */
    @ExcelProperty(value = "存放位置名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("1号库房")
    private String locationName;
    /**
     * 折旧规则名称
     */
    @ExcelProperty(value = "折旧规则名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("配件五年折旧")
    private String depreciationName;
}

