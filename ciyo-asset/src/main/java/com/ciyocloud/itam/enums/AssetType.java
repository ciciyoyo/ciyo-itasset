package com.ciyocloud.itam.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.ciyocloud.common.entity.IDictEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资产类型枚举
 *
 * @author codeck
 * @since 2026-01-01
 */
@Getter
@AllArgsConstructor
public enum AssetType implements IDictEnum<String> {

    /**
     * 设备
     */
    DEVICE("device", "设备"),

    /**
     * 配件
     */
    ACCESSORY("accessory", "配件"),

    /**
     * 耗材
     */
    CONSUMABLE("consumable", "耗材"),

    /**
     * 软件授权
     */
    LICENSE("license", "软件授权"),

    /**
     * 服务
     */
    SERVICE("service", "服务"),

    /**
     * 其他
     */
    OTHER("other", "其他");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    @Override
    public String getValue() {
        return this.code;
    }
}
