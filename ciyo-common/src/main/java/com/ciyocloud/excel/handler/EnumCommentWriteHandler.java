package com.ciyocloud.excel.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.ciyocloud.common.entity.IDictEnum;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.excel.core.EnumSampleProvider;
import org.apache.poi.ss.usermodel.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel 导出时为枚举字段的表头单元格添加注释，显示所有可选值
 *
 * @author codeck
 * @since 2026-01-31
 */
public class EnumCommentWriteHandler implements CellWriteHandler {

    private final Class<?> entityClass;
    private final Map<Integer, String> enumColumnComments = new HashMap<>();
    private boolean initialized = false;

    public EnumCommentWriteHandler(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        // 只处理表头行
        if (!isHead || cell.getRowIndex() != 0) {
            return;
        }

        // 初始化枚举列映射
        if (!initialized) {
            initEnumColumns();
            initialized = true;
        }

        // 检查当前列是否需要添加注释
        int columnIndex = cell.getColumnIndex();
        String commentText = enumColumnComments.get(columnIndex);
        if (commentText != null && !commentText.isEmpty()) {
            Sheet sheet = writeSheetHolder.getSheet();
            Drawing<?> drawing = sheet.getDrawingPatriarch();
            if (drawing == null) {
                drawing = sheet.createDrawingPatriarch();
            }
            addCommentToCell(cell, drawing, commentText);
        }
    }

    private void initEnumColumns() {
        Field[] fields = ReflectUtil.getFields(entityClass);
        int columnIndex = 0;

        for (Field field : fields) {
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            if (excelProperty == null) {
                continue;
            }

            // 检查是否为仅导出字段（不包含在导入模板中）
            ExcelPropertyType excelPropertyType = AnnotationUtil.getAnnotation(field, ExcelPropertyType.class);
            if (ObjectUtil.isNotNull(excelPropertyType) && excelPropertyType.type() == ExcelPropertyType.TypeEnum.EXPORT) {
                continue;
            }

            // 检查是否使用 EnumSampleProvider
            ExcelSample excelSample = field.getAnnotation(ExcelSample.class);
            if (excelSample != null && excelSample.provider() == EnumSampleProvider.class) {
                String enumOptions = getEnumOptionsText(field);
                if (!enumOptions.isEmpty()) {
                    enumColumnComments.put(columnIndex, "可选值: " + enumOptions);
                }
            }

            columnIndex++;
        }
    }

    private void addCommentToCell(Cell cell, Drawing<?> drawing, String commentText) {
        CreationHelper factory = cell.getSheet().getWorkbook().getCreationHelper();

        // 创建注释锚点
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 3);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 3);

        // 创建注释
        Comment comment = drawing.createCellComment(anchor);
        RichTextString richText = factory.createRichTextString(commentText);
        comment.setString(richText);
        comment.setAuthor("系统");

        // 将注释附加到单元格
        cell.setCellComment(comment);
    }

    private String getEnumOptionsText(Field field) {
        Class<?> fieldType = field.getType();
        if (!fieldType.isEnum()) {
            return "";
        }

        try {
            Object[] enumConstants = fieldType.getEnumConstants();
            if (enumConstants == null || enumConstants.length == 0) {
                return "";
            }

            // 如果枚举实现了 IDictEnum，返回所有枚举值的 desc，用 | 分隔
            if (IDictEnum.class.isAssignableFrom(fieldType)) {
                return Arrays.stream(enumConstants)
                        .map(e -> ((IDictEnum<?>) e).getDesc())
                        .collect(Collectors.joining(" | "));
            }

            // 如果是普通枚举，返回所有枚举名称，用 | 分隔
            return Arrays.stream(enumConstants)
                    .map(Object::toString)
                    .collect(Collectors.joining(" | "));

        } catch (Exception e) {
            return "";
        }
    }
}
