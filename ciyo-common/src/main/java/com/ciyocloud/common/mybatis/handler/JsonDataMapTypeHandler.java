package com.ciyocloud.common.mybatis.handler;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.ciyocloud.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * MySQL数据库json处理器
 *
 * @author hubin
 * @since 2019-08-25
 */
@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JsonDataMapTypeHandler extends AbstractJsonTypeHandler<Map<String, Object>> {

    public JsonDataMapTypeHandler(Class<?> type) {
        super(type);
    }

    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, this.toJson(parameter));
    }


    @Override
    public Map<String, Object> parse(String json) {
        return JsonUtils.jsonToMap(json);
    }

    @Override
    public String toJson(Map<String, Object> obj) {
        return JsonUtils.objToJson(obj);
    }
}
