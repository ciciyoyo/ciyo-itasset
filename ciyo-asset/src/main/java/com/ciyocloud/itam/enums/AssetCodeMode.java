package com.ciyocloud.itam.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资产编号生成模式
 */
@Getter
@AllArgsConstructor
public enum AssetCodeMode {
    /**
     * 定长自增模式
     */
    INCREMENT("INCREMENT"),
    /**
     * 时间戳模式
     */
    TIMESTAMP("TIMESTAMP");

    private final String value;
}
