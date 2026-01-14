package com.ciyocloud.itam.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.ciyocloud.common.entity.IDictEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备状态枚举
 */
@Getter
@AllArgsConstructor
public enum DeviceStatus implements IDictEnum<Integer> {

    /**
     * pending
     */
    PENDING(1, "闲置"),

    /**
     * 在用
     */
    DEPLOYED(2, "在用"),

    /**
     * 故障
     */
    FAULT(4, "故障"),

    /**
     * 报废
     */
    SCRAPPED(10, "报废");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String info;

    @Override
    public Integer getValue() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.info;
    }
}
