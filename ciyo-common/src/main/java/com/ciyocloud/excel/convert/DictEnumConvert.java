package com.ciyocloud.excel.convert;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.ciyocloud.common.entity.IDictEnum;
import lombok.extern.slf4j.Slf4j;


/**
 * 字段数据处理
 */
@Slf4j
public class DictEnumConvert implements Converter<Object> {

    @Override
    public Class<Object> supportJavaTypeKey() {
        return Object.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Object convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        // 获取单元格中的字符串值（desc描述）
        String cellValue = cellData.getStringValue();
        if (ObjectUtil.isEmpty(cellValue)) {
            return null;
        }
        
        // 获取字段的实际类型
        Class<?> fieldClass = contentProperty.getField().getType();
        
        // 确保字段类型是枚举且实现了 IDictEnum 接口
        if (!fieldClass.isEnum() || !IDictEnum.class.isAssignableFrom(fieldClass)) {
            log.warn("Field type {} is not an IDictEnum, returning null", fieldClass.getName());
            return null;
        }
        
        // 获取枚举常量并查找匹配的枚举值
        Object[] enumConstants = fieldClass.getEnumConstants();
        for (Object enumConstant : enumConstants) {
            IDictEnum<?> dictEnum = (IDictEnum<?>) enumConstant;
            // 根据desc描述匹配枚举值
            if (cellValue.equals(dictEnum.getDesc())) {
                return enumConstant;
            }
        }
        
        log.warn("No matching enum found for value: {} in enum class: {}", cellValue, fieldClass.getName());
        return null;
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object object, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (ObjectUtil.isNull(object)) {
            return new WriteCellData<>("");
        }
        if (object instanceof IDictEnum) {
            return new WriteCellData<>(((IDictEnum) object).getDesc());
        }
        // 如果是字符串类型（例如从 EnumSampleProvider 返回的示例数据），直接返回
        if (object instanceof String) {
            return new WriteCellData<>((String) object);
        }
        return new WriteCellData<>("");
    }


}


