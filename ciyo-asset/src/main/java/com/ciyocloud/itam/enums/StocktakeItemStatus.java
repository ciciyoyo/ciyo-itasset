package com.ciyocloud.itam.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.ciyocloud.common.entity.IDictEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 盘点状态枚举
 *
 * @author codeck
 * @since 2026-01-01
 */
@Getter
@AllArgsConstructor
public enum StocktakeItemStatus implements IDictEnum<String> {

    /**
     * 正常
     */
    NORMAL("normal", "正常"),

    /**
     * 丢失
     */
    LOST("lost", "丢失"),

    /**
     * 损坏
     */
    DAMAGED("damaged", "损坏"),

    /**
     * 待盘点
     */
    PENDING("pending", "待盘点"),

    /**
     * 报废
     */
    SCRAPPED("scrapped", "报废"),

    /**
     * 盘盈
     */
    SURPLUS("surplus", "盘盈");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    @Override
    public String getValue() {
        return this.code;
    }
}
