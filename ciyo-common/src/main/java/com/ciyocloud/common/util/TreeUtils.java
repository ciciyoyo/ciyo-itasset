package com.ciyocloud.common.util;


import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TreeUtils extends TreeUtil {

    /**
     * 转换树形结构的方法
     *
     * @param <T>       输入节点的泛型类型
     * @param list      输入的对象数组
     * @param keyMapper 键名转换规则
     * @param childKey  父节点键名
     * @return 转换后的树形结构数组
     */
    public static <T> List<Map<String, Object>> convertTreeList(List<T> list, Function<String, String> keyMapper, String childKey) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (T element : list) {
            result.add(convertTree(element, keyMapper, childKey));
        }
        return result;
    }

    /**
     * 递归转换单个节点
     *
     * @param node      输入的单个节点
     * @param keyMapper 键名转换规则
     * @param childKey
     * @return 转换后的节点
     */
    private static Map<String, Object> convertTree(Object node, Function<String, String> keyMapper, String childKey) {
        Map<String, Object> result = new LinkedHashMap<>();
        // 反射获取所有字段
        Field[] fields = ReflectUtil.getFields(node.getClass());
        ;
        for (Field field : fields) {
            field.setAccessible(true); // 允许访问私有字段
            try {
                String originalKey = field.getName();
                String newKey = keyMapper.apply(originalKey);
                Object value = field.get(node);

                // 如果值是数组或列表，递归处理
                if (value != null) {
                    if (childKey.equals(originalKey)) {
                        if (value instanceof List) {
                            List<?> children = (List<?>) value;
                            List<Map<String, Object>> convertedChildren = new ArrayList<>();
                            for (Object child : children) {
                                convertedChildren.add(convertTree(child, keyMapper, childKey));
                            }
                            result.put(newKey, convertedChildren);
                        } else if (value.getClass().isArray()) {
                            Object[] children = (Object[]) value;
                            List<Map<String, Object>> convertedChildren = new ArrayList<>();
                            for (Object child : children) {
                                convertedChildren.add(convertTree(child, keyMapper, childKey));
                            }
                            result.put(newKey, convertedChildren);
                        }
                    } else {
                        result.put(newKey, value);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + field.getName(), e);
            }
        }
        return result;
    }

}
