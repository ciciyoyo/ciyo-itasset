package com.ciyocloud.excel.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 导入模板注册中心：按 code 注册实体类与模板信息，便于统一生成下载。
 */
public final class ExcelImportTemplateRegistry {

    private static final Map<String, Entry> REGISTRY = new ConcurrentHashMap<>();

    private ExcelImportTemplateRegistry() {}

    public static void register(String code, String sheetName, Class<?> clazz) {
        REGISTRY.put(code, new Entry(code, sheetName, clazz, null));
    }

    public static void register(String code, String sheetName, Class<?> clazz, SampleProvider provider) {
        REGISTRY.put(code, new Entry(code, sheetName, clazz, provider));
    }

    public static Entry get(String code) {
        return REGISTRY.get(code);
    }

    public static Map<String, Entry> all() {
        return REGISTRY;
    }

    @FunctionalInterface
    public interface SampleProvider {
        List<?> provide(int count);

        /**
         * 空实现占位
         */
        final class None implements SampleProvider {
            @Override
            public List<?> provide(int count) { return Collections.emptyList(); }
        }
    }

    public static final class Entry {
        public final String code;
        public final String sheetName;
        public final Class<?> clazz;
        public final SampleProvider provider;

        public Entry(String code, String sheetName, Class<?> clazz, SampleProvider provider) {
            this.code = code;
            this.sheetName = sheetName;
            this.clazz = clazz;
            this.provider = provider;
        }
    }
}
