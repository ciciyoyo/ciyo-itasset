package com.ciyocloud.itam.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.excel.annotation.ExcelTemplate;
import com.ciyocloud.itam.entity.ConsumablesEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 耗材VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ExcelIgnoreUnannotated
@ExcelTemplate(code = "consumable", sheetName = "耗材导入模板")
public class ConsumablesVO extends ConsumablesEntity {
    /**
     * 分类名称
     */
    @ExcelProperty(value = "分类名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("打印耗材")
    private String categoryName;
    /**
     * 厂商名称
     */
    @ExcelProperty(value = "厂商名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("惠普")
    private String manufacturerName;
    /**
     * 存放位置名称
     */
    @ExcelProperty(value = "存放位置名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("1号库房")
    private String locationName;
}

