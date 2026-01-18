package com.ciyocloud.itam.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ciyocloud.itam.entity.AllocationsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源分配/领用明细VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ExcelIgnoreUnannotated
public class AllocationsVO extends AllocationsEntity {
    /**
     * 资源名称
     */
    @ExcelProperty(value = "资源名称")
    private String itemName;

    /**
     * 归属者名称
     */
    @ExcelProperty(value = "归属者名称")
    private String ownerName;
}

