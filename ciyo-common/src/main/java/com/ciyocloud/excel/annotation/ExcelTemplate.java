package com.ciyocloud.excel.annotation;

import com.ciyocloud.excel.core.ExcelImportTemplateRegistry;

import java.lang.annotation.*;

/**
 * 实体级模板注册注解，用于自动注册导入模板。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelTemplate {
    /** 唯一 code，用于路由下载 */
    String code();
    /** 模板 sheet 名称/文件前缀 */
    String sheetName();
    /** 可选的模板级示例数据提供者 */
    Class<? extends ExcelImportTemplateRegistry.SampleProvider> provider() default ExcelImportTemplateRegistry.SampleProvider.None.class;
}
