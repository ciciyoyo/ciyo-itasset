package com.ciyocloud.common.helper;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlInjectionUtils;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.ciyocloud.common.util.SpringContextUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;


/**
 * 数据库助手
 *
 * @author codeck
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataBaseHelper {

    private static String dataSourceUrl;


    /**
     * 获取当前数据库链接url
     */
    @SneakyThrows
    public static DbType getDataBaseType() {
        if (StrUtil.isNotBlank(dataSourceUrl)) {
            return JdbcUtils.getDbType(dataSourceUrl);
        }
        Environment environment = SpringContextUtils.getBean(Environment.class);
        String property = environment.getProperty("spring.datasource.url");
        if (StrUtil.isBlank(property)) {
            return DbType.MYSQL;
        }
        dataSourceUrl = property;
        return JdbcUtils.getDbType(property);
    }


    public static boolean isMySql() {
        return DbType.MYSQL == getDataBaseType();
    }

    public static boolean isOracle() {
        return DbType.ORACLE == getDataBaseType() || DbType.ORACLE_12C == getDataBaseType();
    }

    public static boolean isPostgerSql() {
        return DbType.POSTGRE_SQL == getDataBaseType();
    }


    public static boolean isKingBase() {
        return DbType.KINGBASE_ES == getDataBaseType();
    }


    public static boolean isSqlServer() {
        return DbType.SQL_SERVER == getDataBaseType() || DbType.SQL_SERVER2005 == getDataBaseType();
    }


    /**
     * 兼容不同数据库的find_in_set
     *
     * @param var1 值1
     * @param var2 值2
     * @return
     */
    public static String findInSet(Object var1, String var2) {
        DbType dataBaseType = getDataBaseType();
        String var = Convert.toStr(var1);
        if (isSqlServer()) {
            // charindex(',100,' , ',0,100,101,') <> 0
            return "charindex('," + var + ",' , ','+" + var2 + "+',') <> 0";
        } else if (dataBaseType == DbType.POSTGRE_SQL || dataBaseType == DbType.KINGBASE_ES) {
            // (select position(',100,' in ',0,100,101,')) <> 0
            return "(select position('," + var + ",' in ','||" + var2 + "||',')) <> 0";
        } else if (isOracle() || dataBaseType == DbType.DM) {
            // instr(',0,100,101,' , ',100,') <> 0
            return "instr(','||" + var2 + "||',' , '," + var + ",') <> 0";
        }
        // find_in_set(100 , '0,100,101')
        return "find_in_set(" + var + " , " + var2 + ") <> 0";
    }

    /**
     * 兼容不同数据库的时间格式化
     *
     * @param dateColumn 时间字段
     * @param javaFormat Java标准时间格式 (如: yyyy-MM-dd HH:mm:ss)
     * @return 格式化后的SQL语句
     */
    public static String dateFormat(String dateColumn, String javaFormat) {
        DbType dataBaseType = getDataBaseType();
        String dbFormat = convertJavaDateFormat(javaFormat, dataBaseType);

        switch (dataBaseType) {
            case SQL_SERVER:
            case SQL_SERVER2005:
                return "FORMAT(" + dateColumn + ", '" + dbFormat + "')";
            case POSTGRE_SQL:
                return "TO_CHAR(" + dateColumn + ", '" + dbFormat + "')";
            case ORACLE:
            case ORACLE_12C:
            case KINGBASE_ES:
            case DM:
                return "TO_CHAR(" + dateColumn.toUpperCase() + ", '" + dbFormat + "')";
            case MYSQL:
                return "DATE_FORMAT(" + dateColumn + ", '" + dbFormat + "')";
            default:
                throw new UnsupportedOperationException("不支持的数据库类型: " + dataBaseType);
        }
    }


    /**
     * json字段查询
     *
     * @param field    json字段
     * @param subField json子字段
     * @param value    子字段值
     * @return 格式化后的SQL语句
     */
    public static String jsonField(String field, String subField, Object value) {
        Assert.isFalse(SqlInjectionUtils.check(field));
        Assert.isFalse(SqlInjectionUtils.check(subField));
        Assert.isFalse(SqlInjectionUtils.check(value.toString()));
        DbType dataBaseType = getDataBaseType();

        String queryValue = isNumber(value) ? String.valueOf(value) : "'" + value + "'";
        switch (dataBaseType) {
            case MYSQL:
                return StrUtil.format("JSON_EXTRACT({},  '$.\"{}\"') = {}", field, subField, "'" + value + "'");
            case POSTGRE_SQL:
                // PostgreSQL：使用 ->> 操作符，注意子字段必须作为文本常量（带单引号）
                return StrUtil.format("({} ->> '{}') = {}", field, subField, "'" + value + "'");
            case ORACLE:
            case ORACLE_12C:
            case KINGBASE_ES:
            case DM:
                // Oracle：使用 JSON_VALUE 函数，路径格式为 '$.subField'
                return StrUtil.format("JSON_VALUE({}, '$.{}') = {}", field, subField, "'" + value + "'");
            case SQL_SERVER:
            case SQL_SERVER2005:
                // 将所有查询值统一当作字符串处理，确保中文和特殊字符支持
                String formattedValue = "N'" + value + "'";
                // 使用 CAST 统一转换为 NVARCHAR(MAX)
                return StrUtil.format("CAST(JSON_VALUE({}, '$.{}') AS NVARCHAR(MAX)) = {}", field, subField, formattedValue);
            default:
                throw new UnsupportedOperationException("不支持的数据库类型: " + dataBaseType);
        }
    }


    public static boolean isNumber(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Number) {
            return true;
        }
        if (obj instanceof String) {
            try {
                // 尝试将字符串转换为 Double
                Double.parseDouble((String) obj);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    public static String convertField(String field) {
        if (isOracle()) {
            return field.toUpperCase();
        }
        return field;
    }

    /**
     * 将Java日期格式映射为各数据库支持的格式
     *
     * @param javaFormat   Java标准时间格式
     * @param databaseType 数据库类型
     * @return 数据库支持的时间格式
     */
    private static String convertJavaDateFormat(String javaFormat, DbType databaseType) {
        Map<String, String> mapping = new HashMap<>();

        switch (databaseType) {
            case MYSQL:
                mapping.put("yyyy", "%Y");
                mapping.put("MM", "%m");
                mapping.put("dd", "%d");
                mapping.put("HH", "%H");
                mapping.put("mm", "%i");
                mapping.put("ss", "%s");
                break;
            case SQL_SERVER:
            case SQL_SERVER2005:
                mapping.put("yyyy", "yyyy");
                mapping.put("MM", "MM");
                mapping.put("dd", "dd");
                mapping.put("HH", "HH");
                mapping.put("mm", "mm");
                mapping.put("ss", "ss");
                break;
            case POSTGRE_SQL:
            case ORACLE:
            case ORACLE_12C:
            case KINGBASE_ES:
            case DM:
                mapping.put("yyyy", "YYYY");
                mapping.put("MM", "MM");
                mapping.put("dd", "DD");
                mapping.put("HH", "HH24");
                mapping.put("mm", "MI");
                mapping.put("ss", "SS");
                break;
            default:
                throw new UnsupportedOperationException("不支持的数据库类型: " + databaseType);
        }

        // 替换Java格式为数据库格式
        String dbFormat = javaFormat;
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            dbFormat = dbFormat.replace(entry.getKey(), entry.getValue());
        }
        return dbFormat;
    }
}
