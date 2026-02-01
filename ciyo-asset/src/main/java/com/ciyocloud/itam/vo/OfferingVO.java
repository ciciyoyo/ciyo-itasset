package com.ciyocloud.itam.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.excel.annotation.ExcelTemplate;
import com.ciyocloud.itam.entity.OfferingEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务 VO
 *
 * @author codeck
 * @since 2025-12-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ExcelIgnoreUnannotated
@ExcelTemplate(code = "offering", sheetName = "服务导入模板")
public class OfferingVO extends OfferingEntity {

    /**
     * 供应商名称
     */
    @ExcelProperty(value = "供应商名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("阿里云")
    private String supplierName;


}

