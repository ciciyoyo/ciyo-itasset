package com.ciyocloud.itam.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.itam.entity.LicensesEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 软件授权VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ExcelIgnoreUnannotated
public class LicensesVO extends LicensesEntity {
    /**
     * 分类名称
     */
    @ExcelProperty(value = "分类名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("办公软件")
    private String categoryName;
    /**
     * 厂商名称
     */
    @ExcelProperty(value = "厂商名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("微软")
    private String manufacturerName;
    /**
     * 供应商名称
     */
    @ExcelProperty(value = "供应商名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("XX科技")
    private String supplierName;
}

