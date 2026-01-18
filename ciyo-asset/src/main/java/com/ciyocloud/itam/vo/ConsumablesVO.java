package com.ciyocloud.itam.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ciyocloud.itam.entity.ConsumablesEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 耗材VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ExcelIgnoreUnannotated
public class ConsumablesVO extends ConsumablesEntity {
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
     * 存放位置名称
     */
    @ExcelProperty(value = "存放位置名称")
    private String locationName;
}

