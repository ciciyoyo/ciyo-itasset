package com.ciyocloud.excel.core;

import com.ciyocloud.excel.annotation.ExcelTemplate;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

/**
 * 应用启动时，扫描带有 @ExcelTemplate 的实体并自动注册导入模板。
 */
@Component
public class ExcelTemplateRegistrar {

    @PostConstruct
    public void registerTemplates() {
        // 需要扫描的基础包（可按需扩展）
        String[] basePackages = new String[]{
                "com.ciyocloud.system.entity",
                "com.ciyocloud.itam.entity",
                "com.ciyocloud.itam.vo"
        };
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ExcelTemplate.class));
        for (String basePackage : basePackages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                try {
                    Class<?> clazz = Class.forName(bd.getBeanClassName());
                    ExcelTemplate tpl = clazz.getAnnotation(ExcelTemplate.class);
                    Class<? extends ExcelImportTemplateRegistry.SampleProvider> providerClass = tpl.provider();
                    if (providerClass != null && providerClass != ExcelImportTemplateRegistry.SampleProvider.None.class) {
                        ExcelImportTemplateRegistry.SampleProvider provider = providerClass.getDeclaredConstructor().newInstance();
                        ExcelImportTemplateRegistry.register(tpl.code(), tpl.sheetName(), clazz, provider);
                    } else {
                        ExcelImportTemplateRegistry.register(tpl.code(), tpl.sheetName(), clazz);
                    }
                } catch (Exception ignored) {
                    // 如果某个类加载失败，忽略并继续
                }
            }
        }
    }
}
