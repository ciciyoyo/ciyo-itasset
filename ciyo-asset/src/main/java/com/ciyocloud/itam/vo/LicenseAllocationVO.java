package com.ciyocloud.itam.vo;

import com.ciyocloud.itam.entity.AllocationsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 软件分配/领用明细VO
 *
 * @author codeck
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LicenseAllocationVO extends AllocationsEntity {
    /**
     * 归属者名称 (员工姓名或设备名称)
     */
    private String ownerName;
}
