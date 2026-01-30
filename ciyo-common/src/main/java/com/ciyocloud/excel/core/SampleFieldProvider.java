package com.ciyocloud.excel.core;

import java.lang.reflect.Field;

/**
 * 字段级示例生成 Provider 接口。
 */
public interface SampleFieldProvider {
    Object sample(Field field, int index);

    /**
     * 默认空实现，用于标识未提供 Provider。
     */
    final class None implements SampleFieldProvider {
        @Override
        public Object sample(Field field, int index) { return null; }
    }
}
