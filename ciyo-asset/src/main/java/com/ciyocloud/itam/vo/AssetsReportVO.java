package com.ciyocloud.itam.vo;

import com.ciyocloud.itam.enums.AssetType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产报表统计 VO
 */
@Data
public class AssetsReportVO {

    /**
     * 统计月份 (YYYY-MM)
     */
    private String statsMonth;

    /**
     * 资产类型
     */
    private AssetType assetsType;

    /**
     * 资产类型描述
     */
    private String assetsTypeDesc;

    /**
     * 总资产价值
     */
    private BigDecimal totalValue;
}
