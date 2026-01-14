package com.ciyocloud.itam.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.ciyocloud.common.entity.IDictEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 故障状态枚举
 *
 * @author codeck
 * @since 2026-01-01
 */
@Getter
@AllArgsConstructor
public enum FailureStatus implements IDictEnum<String> {

    /**
     * 待处理
     */
    PENDING("pending", "待处理"),

    /**
     * 处理中
     */
    PROCESSING("processing", "处理中"),

    /**
     * 已修复
     */
    RESOLVED("resolved", "已修复"),

    /**
     * 已报废
     */
    SCRAPPED("scrapped", "已报废");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    @Override
    public String getValue() {
        return this.getCode();
    }
}
