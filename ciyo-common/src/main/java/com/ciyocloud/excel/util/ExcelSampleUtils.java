package com.ciyocloud.excel.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.excel.core.SampleFieldProvider;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 基于反射的简易示例数据生成器，用于导入模板演示。
 */
public final class ExcelSampleUtils {

    private ExcelSampleUtils() {}

    @SuppressWarnings("unchecked")
    public static <T> List<T> generateExamples(Class<T> clazz, int count) {
        if (count <= 0) {
            return CollUtil.newArrayList();
        }
        List<T> list = new ArrayList<>(count);
        Field[] fields = ReflectUtil.getFields(clazz);
        for (int i = 0; i < count; i++) {
            T instance = ReflectUtil.newInstance(clazz);
            for (Field field : fields) {
                ExcelPropertyType excelType = field.getAnnotation(ExcelPropertyType.class);
                if (excelType != null && excelType.type() == ExcelPropertyType.TypeEnum.EXPORT) {
                    // 仅导出字段不写入示例
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Class<?> type = field.getType();
                    // 优先使用 @ExcelSample 指定的样例或 Provider
                    Object value = sampleFromAnnotation(field, i);
                    if (value == null) {
                        value = sampleValueForField(type, field.getName(), i);
                    }
                    if (value != null) {
                        field.set(instance, value);
                    }
                } catch (Exception ignored) {
                }
            }
            list.add(instance);
        }
        return list;
    }

    private static Object sampleFromAnnotation(Field field, int i) {
        ExcelSample sample = field.getAnnotation(ExcelSample.class);
        if (sample == null) return null;
        Class<? extends SampleFieldProvider> providerClass = sample.provider();
        if (providerClass != null && providerClass != SampleFieldProvider.None.class) {
            try {
                SampleFieldProvider provider = providerClass.getDeclaredConstructor().newInstance();
                Object v = provider.sample(field, i);
                if (v != null) return v;
            } catch (Exception ignored) {}
        }
        String raw = sample.value();
        if (StrUtil.isBlank(raw)) return null;
        return convertStringToType(raw, field.getType());
    }

    private static Object sampleValueForField(Class<?> type, String name, int i) {
        String lower = name.toLowerCase();
        if (type == String.class) {
            if (StrUtil.containsAny(lower, "id")) return "新增数据时删除该列，填写该列会修改数据";
            if (StrUtil.containsAny(lower, "email")) return "user" + i + "@example.com";
            if (StrUtil.containsAny(lower, "phone", "mobile", "phonenumber")) return "1380000" + String.format("%04d", i);
            if (StrUtil.containsAny(lower, "username", "login", "account")) return "user" + i;
            if (StrUtil.containsAny(lower, "nickname", "name")) return "示例名称" + i;
            if (StrUtil.containsAny(lower, "sex", "gender")) return "男";
            if (StrUtil.containsAny(lower, "status")) return "0";
            if (StrUtil.containsAny(lower, "password")) return "Pass@123";
            if (StrUtil.containsAny(lower, "remark", "desc")) return "示例备注" + i;
            return "示例" + i;
        }
        if (type == Integer.class || type == int.class) return i + 1;
        if (type == Long.class || type == long.class) return 1000L + i;
        if (type == Short.class || type == short.class) return (short) (i + 1);
        if (type == Double.class || type == double.class) return (double) (i + 1);
        if (type == Float.class || type == float.class) return (float) (i + 1);
        if (type == BigDecimal.class) return new BigDecimal("100.00");
        if (type == Boolean.class || type == boolean.class) return Boolean.FALSE;
        if (type == LocalDate.class) return LocalDate.now();
        if (type == LocalDateTime.class) return LocalDateTime.now();
        if (List.class.isAssignableFrom(type)) {
            // 简单处理常见 List<Long> 的场景
            if (StrUtil.containsAny(lower, "role", "post", "ids")) {
                return Arrays.asList(1L, 2L);
            }
            return CollUtil.newArrayList();
        }
        return null;
    }

    private static Object convertStringToType(String raw, Class<?> type) {
        try {
            if (type == String.class) return raw;
            if (type == Integer.class || type == int.class) return Integer.parseInt(raw);
            if (type == Long.class || type == long.class) return Long.parseLong(raw);
            if (type == Short.class || type == short.class) return Short.parseShort(raw);
            if (type == Double.class || type == double.class) return Double.parseDouble(raw);
            if (type == Float.class || type == float.class) return Float.parseFloat(raw);
            if (type == java.math.BigDecimal.class) return new java.math.BigDecimal(raw);
            if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(raw);
            if (type == java.time.LocalDate.class) return java.time.LocalDate.parse(raw);
            if (type == java.time.LocalDateTime.class) return java.time.LocalDateTime.parse(raw);
            if (List.class.isAssignableFrom(type)) {
                // 简易处理以逗号分隔的列表
                String[] arr = raw.split(",");
                return java.util.Arrays.asList(arr);
            }
        } catch (Exception ignored) {}
        return raw;
    }
}
