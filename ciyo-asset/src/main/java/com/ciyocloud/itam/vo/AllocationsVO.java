package com.ciyocloud.itam.vo;

import com.ciyocloud.itam.entity.AllocationsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源分配/领用明细VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AllocationsVO extends AllocationsEntity {
    /**
     * 资源名称
     */
    private String itemName;

    /**
     * 归属者名称
     */
    private String ownerName;
}
