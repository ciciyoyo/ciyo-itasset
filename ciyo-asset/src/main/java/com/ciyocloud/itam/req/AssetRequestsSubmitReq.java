package com.ciyocloud.itam.req;

import com.ciyocloud.itam.enums.AssetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资产申请提交参数
 *
 * @author codeck
 * @since 2026-01-10
 */
@Data
public class AssetRequestsSubmitReq {

    /**
     * 资产类型 (device, license, accessory)
     */
    @NotNull(message = "资产类型不能为空")
    private AssetType itemType;

    /**
     * 申请分类 ID (可选)
     */
    private Long categoryId;

    /**
     * 具体资产 ID (可选)
     */
    private Long itemId;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于0")
    private Integer quantity;

    /**
     * 申请原因
     */
    private String reason;

    /**
     * 是否长期使用
     */
    private Boolean isLongTerm;

    /**
     * 预计归还时间
     */
    private LocalDateTime expectedReturnTime;
}
