package com.ciyocloud.itam.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.excel.annotation.ExcelTemplate;
import com.ciyocloud.itam.entity.ModelsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 型号VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ExcelIgnoreUnannotated
@ExcelTemplate(code = "model", sheetName = "型号导入模板")
public class ModelsVO extends ModelsEntity {
    /**
     * 主键 ID
     */
    @ExcelProperty(value = "型号ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long id;

    /**
     * 型号名称
     */
    @ExcelProperty(value = "型号名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("ThinkPad X1 Carbon")
    private String name;

    /**
     * 型号图片
     */
    @ExcelProperty(value = "型号图片")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("http://example.com/image.jpg")
    private String imageUrl;

    /**
     * 型号编码
     */
    @ExcelProperty(value = "型号编码")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("X1C-2023")
    private String modelNumber;

    /**
     * 报废年限 (月)
     */
    @ExcelProperty(value = "报废年限(月)")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("36")
    private Integer eol;

    /**
     * 分类名称
     */
    @ExcelProperty(value = "分类名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("笔记本电脑")
    private String categoryName;

    /**
     * 厂商名称
     */
    @ExcelProperty(value = "厂商名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("联想")
    private String manufacturerName;

    /**
     * 折旧规则名称
     */
    @ExcelProperty(value = "折旧规则名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("IT设备三年折旧")
    private String depreciationName;
}
