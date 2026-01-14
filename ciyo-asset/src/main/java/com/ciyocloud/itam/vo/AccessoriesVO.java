package com.ciyocloud.itam.vo;

import com.ciyocloud.itam.entity.AccessoriesEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配件VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccessoriesVO extends AccessoriesEntity {
    /**
     * 分类名称
     */
    private String categoryName;
    /**
     * 供应商名称
     */
    private String supplierName;
    /**
     * 厂商名称
     */
    private String manufacturerName;
    /**
     * 存放位置名称
     */
    private String locationName;
    /**
     * 折旧规则名称
     */
    private String depreciationName;
}
