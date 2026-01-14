package com.ciyocloud.itam.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.ciyocloud.common.entity.IDictEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分配状态枚举
 *
 * @author codeck
 * @since 2025-12-31
 */
@Getter
@AllArgsConstructor
public enum AllocationStatus implements IDictEnum<String> {

    /**
     * 预约/规划
     */
    RESERVED("reserved", "预约/规划"),

    /**
     * 占用中
     */
    ACTIVE("active", "占用中"),

    /**
     * 已归还
     */
    RETURNED("returned", "已归还");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    @Override
    public String getValue() {
        return this.code;
    }
}
