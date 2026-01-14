package com.ciyocloud.itam.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.ciyocloud.common.entity.IDictEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务状态枚举
 *
 * @author codeck
 * @since 2025-12-30
 */
@Getter
@AllArgsConstructor
public enum OfferingStatus implements IDictEnum<String> {

    /**
     * 正常
     */
    NORMAL("normal", "正常"),

    /**
     * 异常
     */
    EXCEPTION("exception", "异常"),

    /**
     * 维护中
     */
    MAINTENANCE("maintenance", "维护中"),

    /**
     * 已停止
     */
    STOPPED("stopped", "已停止");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    @Override
    public String getValue() {
        return this.getCode();
    }
}
