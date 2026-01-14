package com.ciyocloud.itam.vo;

import com.ciyocloud.itam.entity.LicensesEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 软件授权VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LicensesVO extends LicensesEntity {
    /**
     * 分类名称
     */
    private String categoryName;
    /**
     * 厂商名称
     */
    private String manufacturerName;
    /**
     * 供应商名称
     */
    private String supplierName;
}
