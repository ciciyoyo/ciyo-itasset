package com.ciyocloud.excel.util;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.fill.FillWrapper;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.ciyocloud.common.util.ServletUtils;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.convert.ExcelBigNumberConvert;
import com.ciyocloud.excel.core.CellMergeStrategy;
import com.ciyocloud.excel.core.DefaultExcelListener;
import com.ciyocloud.excel.core.ExcelListener;
import com.ciyocloud.excel.core.ExcelResult;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Excel相关处理
 *
 * @author Lion Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelUtils {

    /**
     * 同步导入(适用于小数据量)
     *
     * @param is 输入流
     * @return 转换后集合
     */
    public static <T> List<T> importExcel(InputStream is, Class<T> clazz) {
        return EasyExcel.read(is).head(clazz).autoCloseStream(false).sheet().doReadSync();
    }

    /**
     * 根据实体类自动生成并下载导入模板（仅包含标注为 IMPORT/ALL 的字段）
     *
     * @param sheetName 工作表名称/文件前缀
     * @param clazz     实体类
     */
    public static <T> void downloadImportTemplate(String sheetName, Class<T> clazz) {
        try {
            // 仅包含允许导入的字段
            Field[] fields = ReflectUtil.getFields(clazz);
            List<String> includeColumnFieldNames = CollUtil.newArrayList();
            for (Field field : fields) {
                ExcelPropertyType annotation = AnnotationUtil.getAnnotation(field, ExcelPropertyType.class);
                if (ObjectUtil.isNotNull(annotation)) {
                    if (annotation.type() == ExcelPropertyType.TypeEnum.IMPORT || annotation.type() == ExcelPropertyType.TypeEnum.ALL) {
                        includeColumnFieldNames.add(field.getName());
                    }
                }
            }

            HttpServletResponse response = ServletUtils.getResponse();
            resetResponse(sheetName, response);
            ServletOutputStream os = response.getOutputStream();

            // 使用 EasyExcel 写出仅表头的空模板
                ExcelWriterSheetBuilder builder = EasyExcel.write(os, clazz)
                    .autoCloseStream(false)
                    .registerConverter(new ExcelBigNumberConvert())
                    // 列宽自适配（基于表头与示例数据的最长匹配）
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    // 添加枚举注释处理器
                    .registerWriteHandler(new com.ciyocloud.excel.handler.EnumCommentWriteHandler(clazz))
                    // 强制写入表头，避免空数据导致文件无表头
                    .needHead(Boolean.TRUE)
                    // 若未显式标注导入字段，则回退为包含所有注解列
                    .sheet(sheetName);

            if (CollUtil.isNotEmpty(includeColumnFieldNames)) {
                builder.includeColumnFieldNames(includeColumnFieldNames);
            }

            builder.doWrite(CollUtil.newArrayList());
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 根据实体类自动生成并下载导入模板，并附带示例数据
     *
     * @param sheetName 工作表名称/文件前缀
     * @param clazz     实体类
     * @param examples  示例数据列表（可为空）
     */
    public static <T> void downloadImportTemplate(String sheetName, Class<T> clazz, List<T> examples) {
        try {
            // 仅包含允许导入的字段
            Field[] fields = ReflectUtil.getFields(clazz);
            List<String> includeColumnFieldNames = CollUtil.newArrayList();
            for (Field field : fields) {
                ExcelPropertyType annotation = AnnotationUtil.getAnnotation(field, ExcelPropertyType.class);
                if (ObjectUtil.isNotNull(annotation)) {
                    if (annotation.type() == ExcelPropertyType.TypeEnum.IMPORT || annotation.type() == ExcelPropertyType.TypeEnum.ALL) {
                        includeColumnFieldNames.add(field.getName());
                    }
                }
            }

            HttpServletResponse response = ServletUtils.getResponse();
            resetResponse(sheetName, response);
            ServletOutputStream os = response.getOutputStream();

                ExcelWriterSheetBuilder builder = EasyExcel.write(os, clazz)
                    .autoCloseStream(false)
                    .registerConverter(new ExcelBigNumberConvert())
                    // 列宽自适配（基于表头与示例数据的最长匹配）
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    // 添加枚举注释处理器
                    .registerWriteHandler(new com.ciyocloud.excel.handler.EnumCommentWriteHandler(clazz))
                    // 强制写入表头，避免空数据导致文件无表头
                    .needHead(Boolean.TRUE)
                    .sheet(sheetName);

            if (CollUtil.isNotEmpty(includeColumnFieldNames)) {
                builder.includeColumnFieldNames(includeColumnFieldNames);
            }

            List<T> data = CollUtil.isEmpty(examples) ? CollUtil.newArrayList() : examples;
            builder.doWrite(data);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 使用校验监听器 异步导入 同步返回
     *
     * @param is         输入流
     * @param clazz      对象类型
     * @param isValidate 是否 Validator 检验 默认为是
     * @return 转换后集合
     */
    public static <T> ExcelResult<T> importExcel(InputStream is, Class<T> clazz, boolean isValidate) {
        DefaultExcelListener<T> listener = new DefaultExcelListener<>(isValidate);
        EasyExcel.read(is, clazz, listener).sheet().doRead();
        return listener.getExcelResult();
    }

    /**
     * 使用自定义监听器 异步导入 自定义返回
     *
     * @param is       输入流
     * @param clazz    对象类型
     * @param listener 自定义监听器
     * @return 转换后集合
     */
    public static <T> ExcelResult<T> importExcel(InputStream is, Class<T> clazz, ExcelListener<T> listener) {
        EasyExcel.read(is, clazz, listener).sheet().doRead();
        return listener.getExcelResult();
    }

    /**
     * 导出excel
     *
     * @param list      导出数据集合
     * @param sheetName 工作表的名称
     * @param clazz     实体类
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz) {
        HttpServletResponse response = ServletUtils.getResponse();
        exportExcel(list, sheetName, clazz, false, response);
    }


    /**
     * 导出excel
     *
     * @param list      导出数据集合
     * @param sheetName 工作表的名称
     * @param clazz     实体类
     * @param merge     是否合并单元格
     * @param response  响应体
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, boolean merge, HttpServletResponse response) {
        try {
            // 排除掉只导出的字段
            Field[] fields = ReflectUtil.getFields(clazz);
            List<String> excludeColumnFieldNames = CollUtil.newArrayList();
            for (Field field : fields) {
                ExcelPropertyType annotation = AnnotationUtil.getAnnotation(field, ExcelPropertyType.class);
                if (ObjectUtil.isNotNull(annotation) && annotation.type() == ExcelPropertyType.TypeEnum.IMPORT) {
                    excludeColumnFieldNames.add(field.getName());
                }
            }
            resetResponse(sheetName, response);
            ServletOutputStream os = response.getOutputStream();
            ExcelWriterSheetBuilder builder = EasyExcel.write(os, clazz)
                    .autoCloseStream(false)
                    .excludeColumnFieldNames(excludeColumnFieldNames)
                    // 自动适配
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    // 大数值自动转换 防止失真
                    .registerConverter(new ExcelBigNumberConvert())
                    .sheet(sheetName);
            if (merge) {
                // 合并处理器
                builder.registerWriteHandler(new CellMergeStrategy(list, true));
            }
            builder.doWrite(list);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常");
        }
    }


    /**
     * 下载模板
     *
     * @param filename     文件名
     * @param templatePath 模板路径 resource 目录下的路径包括模板文件名
     *                     例如: excel/temp.xlsx
     *                     重点: 模板文件必须放置到启动类对应的 resource 目录下
     */
    public static void downloadTemplate(String filename, String templatePath) {
        try {
            HttpServletResponse response = ServletUtils.getResponse();
            resetResponse(filename, response);
            ClassPathResource templateResource = new ClassPathResource(templatePath);
            InputStream is = templateResource.getStream();
            IoUtil.write(response.getOutputStream(), true, IoUtil.readBytes(is));
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 单表多数据模板导出 模板格式为 {.属性}
     *
     * @param filename     文件名
     * @param templatePath 模板路径 resource 目录下的路径包括模板文件名
     *                     例如: excel/temp.xlsx
     *                     重点: 模板文件必须放置到启动类对应的 resource 目录下
     * @param data         模板需要的数据
     */
    public static void exportTemplate(List<Object> data, String filename, String templatePath) {
        try {
            HttpServletResponse response = ServletUtils.getResponse();
            resetResponse(filename, response);
            ClassPathResource templateResource = new ClassPathResource(templatePath);
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                    .withTemplate(templateResource.getStream())
                    .autoCloseStream(false)
                    // 大数值自动转换 防止失真
                    .registerConverter(new ExcelBigNumberConvert())
                    .build();
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            if (CollUtil.isEmpty(data)) {
                throw new IllegalArgumentException("数据为空");
            }
            // 单表多数据导出 模板格式为 {.属性}
            for (Object d : data) {
                excelWriter.fill(d, writeSheet);
            }
            excelWriter.finish();
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 多表多数据模板导出 模板格式为 {key.属性}
     *
     * @param filename     文件名
     * @param templatePath 模板路径 resource 目录下的路径包括模板文件名
     *                     例如: excel/temp.xlsx
     *                     重点: 模板文件必须放置到启动类对应的 resource 目录下
     * @param data         模板需要的数据
     */
    public static void exportTemplateMultiList(Map<String, Object> data, String filename, String templatePath, HttpServletResponse response) {
        try {
            resetResponse(filename, response);
            ClassPathResource templateResource = new ClassPathResource(templatePath);
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                    .withTemplate(templateResource.getStream())
                    .autoCloseStream(false)
                    // 大数值自动转换 防止失真
                    .registerConverter(new ExcelBigNumberConvert())
                    .build();
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            if (CollUtil.isEmpty(data)) {
                throw new IllegalArgumentException("数据为空");
            }
            for (Map.Entry<String, Object> map : data.entrySet()) {
                // 设置列表后续还有数据
                FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
                if (map.getValue() instanceof Collection) {
                    // 多表导出必须使用 FillWrapper
                    excelWriter.fill(new FillWrapper(map.getKey(), (Collection<?>) map.getValue()), fillConfig, writeSheet);
                } else {
                    excelWriter.fill(map.getValue(), writeSheet);
                }
            }
            excelWriter.finish();
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 重置响应体
     */
    private static void resetResponse(String sheetName, HttpServletResponse response) throws UnsupportedEncodingException {
        String filename = encodingFilename(sheetName);
        response.reset();
        setAttachmentResponseHeader(response, filename);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
    }

    /**
     * 下载文件名重新编码
     *
     * @param response     响应对象
     * @param realFileName 真实文件名
     */
    public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) throws UnsupportedEncodingException {
        String percentEncodedFileName = percentEncode(realFileName);

        String contentDispositionValue = "attachment; filename=" +
                percentEncodedFileName +
                ";" +
                "filename*=" +
                "utf-8''" +
                percentEncodedFileName;

        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
        response.setHeader("Content-disposition", contentDispositionValue);
        response.setHeader("download-filename", percentEncodedFileName);
    }

    /**
     * 百分号编码工具方法
     *
     * @param s 需要百分号编码的字符串
     * @return 百分号编码后的字符串
     */
    public static String percentEncode(String s) throws UnsupportedEncodingException {
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        return encode.replaceAll("\\+", "%20");
    }

    /**
     * 解析导出值 0=男,1=女,2=未知
     *
     * @param propertyValue 参数值
     * @param converterExp  翻译注解
     * @param separator     分隔符
     * @return 解析后值
     */
    public static String convertByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(",");
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StrUtil.containsAny(propertyValue, separator)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[0].equals(value)) {
                        propertyString.append(itemArray[1]).append(separator);
                        break;
                    }
                }
            } else {
                if (itemArray[0].equals(propertyValue)) {
                    return itemArray[1];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 反向解析值 男=0,女=1,未知=2
     *
     * @param propertyValue 参数值
     * @param converterExp  翻译注解
     * @param separator     分隔符
     * @return 解析后值
     */
    public static String reverseByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(",");
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(propertyValue, separator)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[1].equals(value)) {
                        propertyString.append(itemArray[0] + separator);
                        break;
                    }
                }
            } else {
                if (itemArray[1].equals(propertyValue)) {
                    return itemArray[0];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 编码文件名
     */
    public static String encodingFilename(String filename) {
        return IdUtil.fastSimpleUUID() + "_" + filename + ".xlsx";
    }

}
