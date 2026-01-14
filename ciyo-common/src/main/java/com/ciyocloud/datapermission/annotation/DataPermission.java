package com.ciyocloud.datapermission.annotation;

import java.lang.annotation.*;

/**
 * 数据权限组
 *
 * @author Lion Li
 * @version 3.5.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 列配置
     */
    DataColumn[] value();

    /**
     * 数据权限动态启用
     * 比如有的时候需要根据方法的参数来绝对是否增加数据权限
     * 支持el表达式
     */
    String dynamicEnable() default "";
}
