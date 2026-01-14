package com.ciyocloud.itam.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.ciyocloud.common.entity.IDictEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 耗材变动类型枚举
 *
 * @author codeck
 * @since 2025-12-31
 */
@Getter
@AllArgsConstructor
public enum ConsumableActionType implements IDictEnum<String> {

    /**
     * 入库
     */
    STOCK_IN("STOCK_IN", "入库"),

    /**
     * 出库
     */
    STOCK_OUT("STOCK_OUT", "出库");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    @Override
    public String getValue() {
        return this.code;
    }
}
