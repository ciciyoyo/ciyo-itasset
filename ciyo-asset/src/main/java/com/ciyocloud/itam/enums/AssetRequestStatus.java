package com.ciyocloud.itam.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.ciyocloud.common.entity.IDictEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资产申请状态枚举
 *
 * @author codeck
 * @since 2026-01-10
 */
@Getter
@AllArgsConstructor
public enum AssetRequestStatus implements IDictEnum<String> {

    /**
     * 待审批
     */
    PENDING("pending", "待审批"),

    /**
     * 已同意
     */
    APPROVED("approved", "已同意"),

    /**
     * 已拒绝
     */
    REJECTED("rejected", "已拒绝"),

    /**
     * 已取消
     */
    CANCELED("canceled", "已取消");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    @Override
    public String getValue() {
        return this.code;
    }
}
