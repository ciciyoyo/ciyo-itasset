package com.ciyocloud.itam.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ciyocloud.itam.entity.ModelsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 型号VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ExcelIgnoreUnannotated
public class ModelsVO extends ModelsEntity {
    /**
     * 分类名称
     */
    @ExcelProperty(value = "分类名称")
    private String categoryName;
    /**
     * 厂商名称
     */
    @ExcelProperty(value = "厂商名称")
    private String manufacturerName;
    /**
     * 折旧规则名称
     */
    @ExcelProperty(value = "折旧规则名称")
    private String depreciationName;
}
