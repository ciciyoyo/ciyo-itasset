package com.ciyocloud.itam.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.ciyocloud.common.entity.IDictEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 盘点任务状态枚举
 *
 * @author codeck
 * @since 2026-01-01
 */
@Getter
@AllArgsConstructor
public enum StocktakeStatus implements IDictEnum<String> {

    /**
     * 草稿
     */
    DRAFT("draft", "草稿"),

    /**
     * 盘点中
     */
    PROCESSING("processing", "盘点中"),

    /**
     * 已完成
     */
    FINISHED("finished", "已完成"),

    /**
     * 已取消
     */
    CANCELED("canceled", "已取消");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    @Override
    public String getValue() {
        return this.code;
    }
}
