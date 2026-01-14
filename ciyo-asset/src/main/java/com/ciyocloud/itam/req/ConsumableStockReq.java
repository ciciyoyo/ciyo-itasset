package com.ciyocloud.itam.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 耗材库存变动请求
 *
 * @author codeck
 * @since 2026-01-07
 */
@Data
public class ConsumableStockReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 耗材 ID
     */
    @NotNull(message = "耗材ID不能为空")
    private Long consumableId;

    /**
     * 变动数量
     */
    @NotNull(message = "变动数量不能为空")
    @Min(value = 1, message = "变动数量必须大于0")
    private Long quantity;

    /**
     * 目标对象类型
     */
    private String targetType;

    /**
     * 目标对象 ID
     */
    private Long targetId;

    /**
     * 备注
     */
    private String note;
}
