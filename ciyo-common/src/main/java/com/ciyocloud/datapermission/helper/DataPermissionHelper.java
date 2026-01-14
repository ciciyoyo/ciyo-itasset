package com.ciyocloud.datapermission.helper;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据权限助手 (基于 TransmittableThreadLocal)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataPermissionHelper {

    public static final String USER_KEY = "user";
    private static final TransmittableThreadLocal<Map<String, Object>> CONTEXT = new TransmittableThreadLocal<>();

    /**
     * 获取变量
     */
    public static <T> T getVariable(String key) {
        Map<String, Object> context = getContext();
        return (T) context.get(key);
    }

    /**
     * 设置变量
     */
    public static void setVariable(String key, Object value) {
        Map<String, Object> context = getContext();
        context.put(key, value);
    }

    /**
     * 获取数据权限上下文
     */
    public static Map<String, Object> getContext() {
        Map<String, Object> context = CONTEXT.get();
        if (context == null) {
            context = new ConcurrentHashMap<>();
            CONTEXT.set(context);
        }
        return context;
    }

    /**
     * 清除上下文，防止内存泄漏
     */
    public static void clear() {
        CONTEXT.remove();
    }


}
