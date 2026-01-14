package com.ciyocloud.itam.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.ciyocloud.common.entity.IDictEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 耗材关联对象类型枚举
 *
 * @author codeck
 * @since 2025-12-31
 */
@Getter
@AllArgsConstructor
public enum ConsumableTargetType implements IDictEnum<String> {

    /**
     * 采购
     */
    PURCHASE("PURCHASE", "采购"),

    /**
     * 用户
     */
    USER("USER", "用户"),

    /**
     * 资产
     */
    ASSET("ASSET", "资产");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    @Override
    public String getValue() {
        return this.code;
    }
}
