package com.ciyocloud.generator.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.generator.constant.GenConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 业务表 gen_table
 *
 * @author Lion Li
 */
@TableName("gen_table")
public class GenTableEntity extends SysBaseEntity {

    /**
     * 请求参数
     */
    @TableField(exist = false)
    private java.util.Map<String, Object> params;
    /**
     * 编号
     */
    private Long id;
    /**
     * 表名称
     */
    @NotBlank(message = "表名称不能为空")
    private String tableName;
    /**
     * 表描述
     */
    @NotBlank(message = "表描述不能为空")
    private String tableComment;
    /**
     * 关联父表的表名
     */
    private String subTableName;
    /**
     * 本表关联父表的外键名
     */
    private String subTableFkName;
    /**
     * 实体类名称(首字母大写)
     */
    @NotBlank(message = "实体类名称不能为空")
    private String className;
    /**
     * 使用的模板（crud单表操作 tree树表操作 sub主子表操作）
     */
    private String tplCategory;
    /**
     * 生成包路径
     */
    @NotBlank(message = "生成包路径不能为空")
    private String packageName;
    /**
     * 生成模块名
     */
    @NotBlank(message = "生成模块名不能为空")
    private String moduleName;
    /**
     * 生成业务名
     */
    @NotBlank(message = "生成业务名不能为空")
    private String businessName;
    /**
     * 生成功能名
     */
    @NotBlank(message = "生成功能名不能为空")
    private String functionName;
    /**
     * 生成作者
     */
    @NotBlank(message = "作者不能为空")
    private String functionAuthor;
    /**
     * 生成代码方式（0zip压缩包 1自定义路径）
     */
    private String genType;
    /**
     * 生成路径（不填默认项目路径）
     */
    @TableField(updateStrategy = FieldStrategy.NOT_EMPTY)
    private String genPath;
    /**
     * 主键信息
     */
    @TableField(exist = false)
    private GenTableColumnEntity pkColumn;
    /**
     * 子表信息
     */
    @TableField(exist = false)
    private GenTableEntity subTable;
    /**
     * 表列信息
     */
    @Valid
    @TableField(exist = false)
    private List<GenTableColumnEntity> columns;
    /**
     * 其它生成选项
     */
    private String options;
    /**
     * 备注
     */
    private String remark;
    /**
     * 树编码字段
     */
    @TableField(exist = false)
    private String treeCode;
    /**
     * 树父编码字段
     */
    @TableField(exist = false)
    private String treeParentCode;
    /**
     * 树名称字段
     */
    @TableField(exist = false)
    private String treeName;
    /*
     * 菜单id列表
     */
    @TableField(exist = false)
    private List<Long> menuIds;
    /**
     * 上级菜单ID字段
     */
    @TableField(exist = false)
    private String parentMenuId;
    /**
     * 上级菜单名称字段
     */
    @TableField(exist = false)
    private String parentMenuName;

    public static boolean isSub(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_SUB, tplCategory);
    }

    public static boolean isTree(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_TREE, tplCategory);
    }

    public static boolean isCrud(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_CRUD, tplCategory);
    }

    public static boolean isSuperColumn(String tplCategory, String javaField) {
        if (isTree(tplCategory)) {
            return StringUtils.equalsAnyIgnoreCase(javaField,
                    ArrayUtils.addAll(GenConstants.TREE_ENTITY, GenConstants.BASE_ENTITY));
        }
        return StringUtils.equalsAnyIgnoreCase(javaField, GenConstants.BASE_ENTITY);
    }

    public java.util.Map<String, Object> getParams() {
        if (params == null) {
            params = new java.util.HashMap<>();
        }
        return params;
    }

    public void setParams(java.util.Map<String, Object> params) {
        this.params = params;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public String getSubTableName() {
        return subTableName;
    }

    public void setSubTableName(String subTableName) {
        this.subTableName = subTableName;
    }

    public String getSubTableFkName() {
        return subTableFkName;
    }

    public void setSubTableFkName(String subTableFkName) {
        this.subTableFkName = subTableFkName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTplCategory() {
        return tplCategory;
    }

    public void setTplCategory(String tplCategory) {
        this.tplCategory = tplCategory;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionAuthor() {
        return functionAuthor;
    }

    public void setFunctionAuthor(String functionAuthor) {
        this.functionAuthor = functionAuthor;
    }

    public String getGenType() {
        return genType;
    }

    public void setGenType(String genType) {
        this.genType = genType;
    }

    public String getGenPath() {
        return genPath;
    }

    public void setGenPath(String genPath) {
        this.genPath = genPath;
    }

    public GenTableColumnEntity getPkColumn() {
        return pkColumn;
    }

    public void setPkColumn(GenTableColumnEntity pkColumn) {
        this.pkColumn = pkColumn;
    }

    public GenTableEntity getSubTable() {
        return subTable;
    }

    public void setSubTable(GenTableEntity subTable) {
        this.subTable = subTable;
    }

    public List<GenTableColumnEntity> getColumns() {
        return columns;
    }

    public void setColumns(List<GenTableColumnEntity> columns) {
        this.columns = columns;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTreeCode() {
        return treeCode;
    }

    public void setTreeCode(String treeCode) {
        this.treeCode = treeCode;
    }

    public String getTreeParentCode() {
        return treeParentCode;
    }

    public void setTreeParentCode(String treeParentCode) {
        this.treeParentCode = treeParentCode;
    }

    public String getTreeName() {
        return treeName;
    }

    public void setTreeName(String treeName) {
        this.treeName = treeName;
    }

    public List<Long> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }

    public String getParentMenuId() {
        return parentMenuId;
    }

    public void setParentMenuId(String parentMenuId) {
        this.parentMenuId = parentMenuId;
    }

    public String getParentMenuName() {
        return parentMenuName;
    }

    public void setParentMenuName(String parentMenuName) {
        this.parentMenuName = parentMenuName;
    }

    public boolean isSub() {
        return isSub(this.tplCategory);
    }

    public boolean isTree() {
        return isTree(this.tplCategory);
    }

    public boolean isCrud() {
        return isCrud(this.tplCategory);
    }

    public boolean isSuperColumn(String javaField) {
        return isSuperColumn(this.tplCategory, javaField);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenTableEntity)) return false;
        if (!super.equals(o)) return false;
        GenTableEntity that = (GenTableEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(tableName, that.tableName) &&
                Objects.equals(tableComment, that.tableComment) &&
                Objects.equals(subTableName, that.subTableName) &&
                Objects.equals(subTableFkName, that.subTableFkName) &&
                Objects.equals(className, that.className) &&
                Objects.equals(tplCategory, that.tplCategory) &&
                Objects.equals(packageName, that.packageName) &&
                Objects.equals(moduleName, that.moduleName) &&
                Objects.equals(businessName, that.businessName) &&
                Objects.equals(functionName, that.functionName) &&
                Objects.equals(functionAuthor, that.functionAuthor) &&
                Objects.equals(genType, that.genType) &&
                Objects.equals(genPath, that.genPath) &&
                Objects.equals(options, that.options) &&
                Objects.equals(remark, that.remark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, tableName, tableComment, subTableName, subTableFkName, className, tplCategory, packageName, moduleName, businessName, functionName, functionAuthor, genType, genPath, options, remark);
    }
}
