package com.ciyocloud.excel.annotation;

import java.lang.annotation.*;


/**
 * 自定义字段注解，
 * 控制一个实体类支持导入导出
 *
 * @author codeck
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelPropertyType {


    TypeEnum type() default TypeEnum.ALL;


    enum TypeEnum {
        /**
         * 导出
         */
        EXPORT,
        /**
         * 导入
         */
        IMPORT,
        /**
         * 导入导出
         */
        ALL
    }
}
