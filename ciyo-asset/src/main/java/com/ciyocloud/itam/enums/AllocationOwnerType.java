package com.ciyocloud.itam.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.ciyocloud.common.entity.IDictEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分配归属类型枚举
 *
 * @author codeck
 * @since 2025-12-31
 */
@Getter
@AllArgsConstructor
public enum AllocationOwnerType implements IDictEnum<String> {

    /**
     * 员工
     */
    USER("user", "员工"),

    /**
     * 部门/地点
     */
    LOCATION("location", "地点/部门"),

    /**
     * 设备
     */
    ASSET("asset", "设备");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    @Override
    public String getValue() {
        return this.code;
    }
}
