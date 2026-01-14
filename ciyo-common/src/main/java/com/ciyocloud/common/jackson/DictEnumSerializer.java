package com.ciyocloud.common.jackson;

import com.ciyocloud.common.entity.IDictEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;


/**
 * 字典枚举序列化定义
 *
 * @author codeck
 */
public class DictEnumSerializer extends JsonSerializer<IDictEnum> {

    @Override
    public void serialize(IDictEnum dictEnum, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(dictEnum.getValue());
        jsonGenerator.writeFieldName(jsonGenerator.getOutputContext().getCurrentName() + "Desc");
        jsonGenerator.writeString(dictEnum.getDesc());
    }


    @Override
    public Class handledType() {
        return IDictEnum.class;
    }

}
