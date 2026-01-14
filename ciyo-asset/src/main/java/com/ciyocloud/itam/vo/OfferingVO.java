package com.ciyocloud.itam.vo;

import com.ciyocloud.itam.entity.OfferingEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 服务 VO
 *
 * @author codeck
 * @since 2025-12-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OfferingVO extends OfferingEntity {

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 关联目标名称 (资产/配件/耗材/软件授权)
     */
    private String targetName;

    /**
     * 分配/关联时间
     */
    private LocalDateTime assignDate;
}
