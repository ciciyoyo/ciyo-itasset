package com.ciyocloud.excel.annotation;

import com.ciyocloud.excel.core.SampleFieldProvider;

import java.lang.annotation.*;

/**
 * 为导入模板提供字段级示例数据。
 * 用法：
 *  - 简单值：@ExcelSample("demo@example.com")
 *  - 使用 Provider：@ExcelSample(provider = MyProvider.class)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelSample {
    /**
     * 示例值（字符串形式），会按字段类型自动转换。
     */
    String value() default "";

    /**
     * 可选的示例生成器 Provider，实现更复杂的示例逻辑。
     */
    Class<? extends SampleFieldProvider> provider() default SampleFieldProvider.None.class;
}
