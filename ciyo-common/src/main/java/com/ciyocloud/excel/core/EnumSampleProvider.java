package com.ciyocloud.excel.core;

import com.ciyocloud.common.entity.IDictEnum;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 枚举字段示例生成 Provider。
 * 为枚举字段返回第一个枚举值作为示例对象，并修改表头以包含所有可选值。
 *
 * @author codeck
 * @since 2026-01-31
 */
public class EnumSampleProvider implements SampleFieldProvider {

    /**
     * 获取枚举字段的所有可选值描述，用于在表头或注释中显示
     */
    public static String getEnumOptionsText(Field field) {
        Class<?> fieldType = field.getType();
        if (!fieldType.isEnum()) {
            return "";
        }

        try {
            Object[] enumConstants = fieldType.getEnumConstants();
            if (enumConstants == null || enumConstants.length == 0) {
                return "";
            }

            // 如果枚举实现了 IDictEnum，返回所有枚举值的 desc，用 | 分隔
            if (IDictEnum.class.isAssignableFrom(fieldType)) {
                return Arrays.stream(enumConstants)
                        .map(e -> ((IDictEnum<?>) e).getDesc())
                        .collect(Collectors.joining("|"));
            }

            // 如果是普通枚举，返回所有枚举名称，用 | 分隔
            return Arrays.stream(enumConstants)
                    .map(Object::toString)
                    .collect(Collectors.joining("|"));

        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public Object sample(Field field, int index) {
        Class<?> fieldType = field.getType();

        // 检查字段类型是否为枚举
        if (!fieldType.isEnum()) {
            return null;
        }

        try {
            // 获取枚举的所有常量
            Object[] enumConstants = fieldType.getEnumConstants();
            if (enumConstants == null || enumConstants.length == 0) {
                return null;
            }

            // 返回第一个枚举值作为示例（这样可以正确设置到字段中）
            return enumConstants[0];

        } catch (Exception e) {
            return null;
        }
    }
}
