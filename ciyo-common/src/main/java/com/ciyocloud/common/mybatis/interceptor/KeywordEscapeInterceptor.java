package com.ciyocloud.common.mybatis.interceptor;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.ciyocloud.common.helper.DataBaseHelper;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;

import java.sql.Connection;
import java.util.*;

import static com.baomidou.mybatisplus.annotation.DbType.*;

/**
 * 表字段转义拦截器
 * 有些字段在某些数据库可能会是关键字 改起来比较麻烦 这里我们使用拦截器进行转义
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class KeywordEscapeInterceptor implements InnerInterceptor {

    /**
     * 存储数据库关键字映射:
     * key -> 数据库类型 (mysql, oracle等)
     * value -> 每个数据库的 {表名 -> 关键字字段集合}
     */
    private static final Map<DbType, Map<String, Set<String>>> DB_KEYWORDS = new HashMap<>();

    static {
        // MySQL 关键字映射
        Map<String, Set<String>> mysqlTables = new HashMap<>();

        // Oracle 关键字映射
        Map<String, Set<String>> oracleTables = new HashMap<>();
        oracleTables.put("sys_post", new HashSet<>(Arrays.asList("level"))); // table1 表的 order、level 需要加引号

        // 存入关键字映射
        DB_KEYWORDS.put(MYSQL, mysqlTables);
        DB_KEYWORDS.put(ORACLE, oracleTables);
        DB_KEYWORDS.put(KINGBASE_ES, oracleTables);
        DB_KEYWORDS.put(POSTGRE_SQL, oracleTables);
    }

    // SQL 关键字转义符号，默认 MySQL
    private String escapeStart = "`";
    private String escapeEnd = "`";
    private boolean upperCaseForOracle = false;

    // 存储当前数据库的关键字映射 (表名 -> 关键字段)
    private Map<String, Set<String>> currentDbKeywords = new HashMap<>();

    public KeywordEscapeInterceptor() {
        // 自动检测数据库类型，并切换关键字配置
        setDatabaseConfig();
    }

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        PluginUtils.MPStatementHandler mpSh = PluginUtils.mpStatementHandler(sh);
        try {
            PluginUtils.MPBoundSql mpBs = mpSh.mPBoundSql();
            // 解析 SQL 语句
            Statement stmt = CCJSqlParserUtil.parse(mpBs.sql());
            if (stmt instanceof Select) {
                processSelect((Select) stmt);
            }
            // 设置修改后的 SQL 语句
            mpBs.sql(stmt.toString());
        } catch (Exception e) {
            // 解析失败则不修改 SQL，防止影响查询
        }
    }

    /**
     * 处理 SELECT 语句，给关键字字段加上引号
     */
    private void processSelect(Select select) {
        if (select.getPlainSelect() != null) {
            PlainSelect plainSelect = select.getPlainSelect();

            // 获取表名
            String tableName = getTableName(plainSelect);
            if (tableName == null || !currentDbKeywords.containsKey(tableName)) {
                return; // 当前表没有关键字，不需要修改 SQL
            }

            Set<String> tableKeywords = currentDbKeywords.get(tableName);

            // 遍历 SELECT 语句中的列
            List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
            if (selectItems != null) {
                for (int i = 0; i < selectItems.size(); i++) {
                    SelectItem item = selectItems.get(i);
                    SelectItem newItem = processSelectItem(item, tableKeywords);
                    if (newItem != null) {
                        selectItems.set(i, newItem);
                    }
                }
            }
        } else if (select.getSetOperationList() != null) {
            SetOperationList setOperationList = select.getSetOperationList();
            List<Select> selectList = setOperationList.getSelects();
            selectList.forEach(this::processSelect);
        }
    }

    /**
     * 处理单个 SelectItem，检查是否需要转义关键字
     */
    private SelectItem processSelectItem(SelectItem item, Set<String> tableKeywords) {
        try {
            // 使用反射检查是否是 SelectExpressionItem 类型
            Class<?> itemClass = item.getClass();
            String className = itemClass.getSimpleName();

            // 检查是否是表达式类型的 SelectItem
            if (className.contains("Expression") || className.contains("Column")) {
                // 尝试获取 expression 字段
                try {
                    java.lang.reflect.Method getExpressionMethod = itemClass.getMethod("getExpression");
                    Object expressionObj = getExpressionMethod.invoke(item);

                    if (expressionObj instanceof Column) {
                        Column column = (Column) expressionObj;
                        String columnName = column.getColumnName();

                        // 如果该列是关键字，则加引号
                        if (tableKeywords.contains(columnName.toLowerCase())) {
                            if (upperCaseForOracle) {
                                columnName = columnName.toUpperCase();
                            }
                            column.setColumnName(escapeStart + columnName + escapeEnd);
                        }
                    }
                } catch (Exception e) {
                    // 如果反射失败，忽略这个 item
                }
            }
        } catch (Exception e) {
            // 如果处理失败，返回原 item
        }

        return item;
    }

    /**
     * 获取 SELECT 语句中的表名
     */
    private String getTableName(PlainSelect plainSelect) {
        if (plainSelect.getFromItem() instanceof net.sf.jsqlparser.schema.Table) {
            return ((net.sf.jsqlparser.schema.Table) plainSelect.getFromItem()).getName().toLowerCase();
        }
        return null;
    }

    /**
     * 根据数据库类型设置关键字转义符
     */
    private void setDatabaseConfig() {
        DbType dbType = DataBaseHelper.getDataBaseType();
        switch (dbType) {
            case ORACLE:
            case ORACLE_12C:
                this.escapeStart = "\"";
                this.escapeEnd = "\"";
                this.upperCaseForOracle = true;
                this.currentDbKeywords = DB_KEYWORDS.getOrDefault(ORACLE, new HashMap<>());
                break;
            case POSTGRE_SQL:
            case KINGBASE_ES:
                this.escapeStart = "\"";
                this.escapeEnd = "\"";
                this.upperCaseForOracle = false;
                this.currentDbKeywords = DB_KEYWORDS.getOrDefault(POSTGRE_SQL, new HashMap<>());
                break;
            case SQL_SERVER:
                this.escapeStart = "[";
                this.escapeEnd = "]";
                this.upperCaseForOracle = false;
                this.currentDbKeywords = DB_KEYWORDS.getOrDefault(SQL_SERVER, new HashMap<>());
                break;
            case MYSQL:
            default:
                this.escapeStart = "`";
                this.escapeEnd = "`";
                this.upperCaseForOracle = false;
                this.currentDbKeywords = DB_KEYWORDS.getOrDefault(MYSQL, new HashMap<>());
                break;
        }
    }
}
