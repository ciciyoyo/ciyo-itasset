package com.ciyocloud.generator.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static com.ciyocloud.generator.constant.GenConstants.TYPE_INTEGER;
import static com.ciyocloud.generator.constant.GenConstants.TYPE_LONG;

/**
 * 代码生成业务字段表 gen_table_column
 *
 * @author Lion Li
 */
@TableName("gen_table_column")
public class GenTableColumnEntity extends SysBaseEntity {

    /**
     * 编号
     */
    private Long id;
    /**
     * 归属表编号
     */
    private Long tableId;
    /**
     * 列名称
     */
    private String columnName;
    /**
     * 列描述
     */
    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS, jdbcType = org.apache.ibatis.type.JdbcType.VARCHAR)
    private String columnComment;
    /**
     * 列类型
     */
    private String columnType;
    /**
     * JAVA类型
     */
    private String javaType;
    /**
     * js类型
     */
    @TableField(exist = false)
    private String jsType;
    /**
     * JAVA字段名
     */
    @NotBlank(message = "Java属性不能为空")
    private String javaField;
    /**
     * 是否主键（1是）
     */
    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS, jdbcType = org.apache.ibatis.type.JdbcType.VARCHAR)
    private String isPk;
    /**
     * 是否自增（1是）
     */
    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS, jdbcType = org.apache.ibatis.type.JdbcType.VARCHAR)
    private String isIncrement;
    /**
     * 是否必填（1是）
     */
    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS, jdbcType = org.apache.ibatis.type.JdbcType.VARCHAR)
    private String isRequired;
    /**
     * 是否为插入字段（1是）
     */
    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS, jdbcType = org.apache.ibatis.type.JdbcType.VARCHAR)
    private String isInsert;
    /**
     * 是否编辑字段（1是）
     */
    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS, jdbcType = org.apache.ibatis.type.JdbcType.VARCHAR)
    private String isEdit;
    /**
     * 是否列表字段（1是）
     */
    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS, jdbcType = org.apache.ibatis.type.JdbcType.VARCHAR)
    private String isList;
    /**
     * 是否查询字段（1是）
     */
    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS, jdbcType = org.apache.ibatis.type.JdbcType.VARCHAR)
    private String isQuery;
    /**
     * 查询方式（EQ等于、NE不等于、GT大于、LT小于、LIKE模糊、BETWEEN范围）
     */
    private String queryType;
    /**
     * 显示类型（input文本框、textarea文本域、select下拉框、checkbox复选框、radio单选框、datetime日期控件、image图片上传控件、upload文件上传控件、editor富文本控件）
     */
    private String htmlType;
    /**
     * 字典类型
     */
    private String dictType;
    /**
     * 排序
     */
    private Integer sort;

    public static boolean isSuperColumn(String javaField) {
        return StringUtils.equalsAnyIgnoreCase(javaField,
                // BaseEntity
                "createBy", "createTime", "updateBy", "updateTime",
                // TreeEntity
                "parentName", "parentId");
    }

    public static boolean isUsableColumn(String javaField) {
        // isSuperColumn()中的名单用于避免生成多余Domain属性，若某些属性在生成页面时需要用到不能忽略，则放在此处白名单
        return StringUtils.equalsAnyIgnoreCase(javaField, "parentId", "orderNum", "remark");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getJavaField() {
        return javaField;
    }

    public void setJavaField(String javaField) {
        this.javaField = javaField;
    }

    public String getIsPk() {
        return isPk;
    }

    public void setIsPk(String isPk) {
        this.isPk = isPk;
    }

    public String getIsIncrement() {
        return isIncrement;
    }

    public void setIsIncrement(String isIncrement) {
        this.isIncrement = isIncrement;
    }

    public String getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(String isRequired) {
        this.isRequired = isRequired;
    }

    public String getIsInsert() {
        return isInsert;
    }

    public void setIsInsert(String isInsert) {
        this.isInsert = isInsert;
    }

    public String getIsEdit() {
        return isEdit;
    }

    public void setIsEdit(String isEdit) {
        this.isEdit = isEdit;
    }

    public String getIsList() {
        return isList;
    }

    public void setIsList(String isList) {
        this.isList = isList;
    }

    public String getIsQuery() {
        return isQuery;
    }

    public void setIsQuery(String isQuery) {
        this.isQuery = isQuery;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getHtmlType() {
        return htmlType;
    }

    public void setHtmlType(String htmlType) {
        this.htmlType = htmlType;
    }

    public String getDictType() {
        return dictType;
    }

    public void setDictType(String dictType) {
        this.dictType = dictType;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getCapJavaField() {
        return StringUtils.capitalize(javaField);
    }

    public boolean isPk() {
        return isPk(this.isPk);
    }

    public boolean isPk(String isPk) {
        return isPk != null && StringUtils.equals("1", isPk);
    }

    public boolean isIncrement() {
        return isIncrement(this.isIncrement);
    }

    public boolean isIncrement(String isIncrement) {
        return isIncrement != null && StringUtils.equals("1", isIncrement);
    }

    public boolean isRequired() {
        return isRequired(this.isRequired);
    }

    public boolean isRequired(String isRequired) {
        return isRequired != null && StringUtils.equals("1", isRequired);
    }

    public boolean isInsert() {
        return isInsert(this.isInsert);
    }

    public boolean isInsert(String isInsert) {
        return isInsert != null && StringUtils.equals("1", isInsert);
    }

    public boolean isEdit() {
        return isEdit(this.isEdit);
    }

    public boolean isEdit(String isEdit) {
        return isEdit != null && StringUtils.equals("1", isEdit);
    }

    public boolean isList() {
        return isList(this.isList);
    }

    public boolean isList(String isList) {
        return isList != null && StringUtils.equals("1", isList);
    }

    public boolean isQuery() {
        return isQuery(this.isQuery);
    }

    public boolean isQuery(String isQuery) {
        return isQuery != null && StringUtils.equals("1", isQuery);
    }

    public boolean isSuperColumn() {
        return isSuperColumn(this.javaField);
    }

    public boolean isUsableColumn() {
        return isUsableColumn(javaField);
    }

    public String readConverterExp() {
        String remarks = StringUtils.substringBetween(this.columnComment, "（", "）");
        StringBuffer sb = new StringBuffer();
        if (StringUtils.isNotEmpty(remarks)) {
            for (String value : remarks.split(" ")) {
                if (StringUtils.isNotEmpty(value)) {
                    Object startStr = value.subSequence(0, 1);
                    String endStr = value.substring(1);
                    sb.append(StringUtils.EMPTY).append(startStr).append("=").append(endStr).append(",");
                }
            }
            return sb.deleteCharAt(sb.length() - 1).toString();
        } else {
            return this.columnComment;
        }
    }

    public String getJsType() {
        if (StrUtil.containsAny(this.jsType, TYPE_LONG, TYPE_INTEGER)) {
            return "number";
        } else {
            return "string";
        }
    }

    public void setJsType(String jsType) {
        this.jsType = jsType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenTableColumnEntity)) return false;
        if (!super.equals(o)) return false;
        GenTableColumnEntity that = (GenTableColumnEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(tableId, that.tableId) &&
                Objects.equals(columnName, that.columnName) &&
                Objects.equals(columnComment, that.columnComment) &&
                Objects.equals(columnType, that.columnType) &&
                Objects.equals(javaType, that.javaType) &&
                Objects.equals(javaField, that.javaField) &&
                Objects.equals(isPk, that.isPk) &&
                Objects.equals(isIncrement, that.isIncrement) &&
                Objects.equals(isRequired, that.isRequired) &&
                Objects.equals(isInsert, that.isInsert) &&
                Objects.equals(isEdit, that.isEdit) &&
                Objects.equals(isList, that.isList) &&
                Objects.equals(isQuery, that.isQuery) &&
                Objects.equals(queryType, that.queryType) &&
                Objects.equals(htmlType, that.htmlType) &&
                Objects.equals(dictType, that.dictType) &&
                Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, tableId, columnName, columnComment, columnType, javaType, javaField, isPk, isIncrement, isRequired, isInsert, isEdit, isList, isQuery, queryType, htmlType, dictType, sort);
    }
}
