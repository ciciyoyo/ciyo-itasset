package com.ciyocloud.excel.convert;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 转换成List
 *
 * @author codeck
 */
@Slf4j
public class ExcelLongListConvert implements Converter<List> {

    @Override
    public Class<List> supportJavaTypeKey() {
        return List.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public List convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (ObjectUtil.isNotNull(cellData.getStringValue())) {
            return StrUtil.split(cellData.getStringValue(), ",").stream().map(Long::valueOf).collect(Collectors.toList());
        } else if (ObjectUtil.isNotNull(cellData.getNumberValue())) {
            return ListUtil.of(cellData.getNumberValue().longValue());
        }
        return null;
    }

    @Override
    public WriteCellData<List> convertToExcelData(List object, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (ObjectUtil.isNull(object)) {
            return new WriteCellData<>("");
        }
        return new WriteCellData<>(StrUtil.join(",", object));
    }


}
