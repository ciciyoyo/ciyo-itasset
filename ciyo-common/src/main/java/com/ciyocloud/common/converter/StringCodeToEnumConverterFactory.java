package com.ciyocloud.common.converter;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ciyocloud.common.entity.IDictEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.Map;


/**
 * 枚举转换器
 */
@Slf4j
public class StringCodeToEnumConverterFactory implements ConverterFactory<String, IDictEnum> {
    private static final Map<Class, Converter> CONVERTERS = MapUtil.newConcurrentHashMap();

    /**
     * 获取一个从 String 转化为 T 的转换器，T 是一个泛型，有多个实现
     *
     * @param targetType 转换后的类型
     * @return 返回一个转化器
     */
    @Override
    public <T extends IDictEnum> Converter<String, T> getConverter(Class<T> targetType) {
        Converter<String, T> converter = CONVERTERS.get(targetType);
        if (converter == null) {
            converter = new ToEnumConverter<>(targetType);
            CONVERTERS.put(targetType, converter);
        }
        return converter;
    }

    public static class ToEnumConverter<T extends IDictEnum> implements Converter<String, T> {
        private Map<String, T> enumMap = MapUtil.newConcurrentHashMap();

        public ToEnumConverter(Class<T> enumType) {
            T[] enums = enumType.getEnumConstants();
            for (T e : enums) {
                enumMap.put(e.getValue().toString(), e);
            }
        }


        @Override
        public T convert(String source) {
            T t = enumMap.get(source);
            if (ObjectUtil.isNull(t)) {
                throw new IllegalArgumentException("无法匹配对应的枚举类型");
            }
            return t;
        }
    }
}
