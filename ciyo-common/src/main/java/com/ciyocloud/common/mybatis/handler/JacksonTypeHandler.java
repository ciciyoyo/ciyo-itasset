package com.ciyocloud.common.mybatis.handler;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.ciyocloud.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * Jackson 实现 JSON 字段类型处理器
 *
 * @author hubin
 * @since 2019-08-25
 */
@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JacksonTypeHandler extends AbstractJsonTypeHandler<Object> {
    private final Class<?> type;

    public JacksonTypeHandler(Class<?> type) {
        super(type);
        if (log.isTraceEnabled()) {
            log.trace("JacksonTypeHandler(" + type + ")");
        }
        Assert.notNull(type, "Type argument cannot be null");
        this.type = type;
    }


    @Override
    public Object parse(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return JsonUtils.jsonToObj(json, type);
    }

    @Override
    public String toJson(Object obj) {
        try {
            return JsonUtils.objToJson(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
