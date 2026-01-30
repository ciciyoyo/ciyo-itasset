package com.ciyocloud.api.web.controller.common;

import com.ciyocloud.excel.core.ExcelImportTemplateRegistry;
import com.ciyocloud.excel.util.ExcelSampleUtils;
import com.ciyocloud.excel.util.ExcelUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通用 Excel 模板下载控制器
 */
@RestController
public class ExcelTemplateController {

    /**
     * 通过注册的 code 下载对应的导入模板
     * 例如：/common/excel/template/sysUser
     */
    @GetMapping("/common/excel/template/{code}")
    public void downloadImportTemplate(@PathVariable("code") String code,
                                       @RequestParam(value = "examples", required = false, defaultValue = "0") int examples) {
        ExcelImportTemplateRegistry.Entry entry = ExcelImportTemplateRegistry.get(code);
        if (entry == null) {
            throw new IllegalArgumentException("未找到导入模板注册: " + code);
        }
        if (examples > 0) {
            List<?> data;
            if (entry.provider != null) {
                data = entry.provider.provide(examples);
            } else {
                data = ExcelSampleUtils.generateExamples(entry.clazz, examples);
            }
            //noinspection unchecked
            ExcelUtils.downloadImportTemplate(entry.sheetName, (Class<Object>) entry.clazz, (List<Object>) data);
        } else {
            ExcelUtils.downloadImportTemplate(entry.sheetName, entry.clazz);
        }
    }
}
