package com.ciyocloud.system.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.OrderBy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.common.jackson.LongToStringSerializer;
import com.ciyocloud.common.mybatis.handler.BooleanTypeHandler;
import com.ciyocloud.common.util.ShortIdUtils;
import com.ciyocloud.excel.annotation.ExcelDictFormat;
import com.ciyocloud.excel.convert.ExcelDictConvert;
import com.ciyocloud.system.constant.SystemConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色表 sys_role
 *
 * @author codeck
 */
@Data
@ExcelIgnoreUnannotated
@TableName("sys_role")
@NoArgsConstructor
public class SysRoleEntity extends SysBaseEntity {

    /**
     * 角色ID
     */
    @ExcelProperty(value = "角色序号")
    @JsonSerialize(using = LongToStringSerializer.class)
    private Long id;

    /**
     * 角色名称
     */
    @ExcelProperty(value = "角色名称")
    @NotBlank(message = "{system.role.name.notnull}")
    @Size(min = 0, max = 30, message = "{system.role.name.error}")
    private String roleName;

    /**
     * 角色权限
     */
    @ExcelProperty(value = "角色权限")
//    @NotBlank(message = "{system.role.key.notnull}")
//    @Size(min = 0, max = 100, message = "{system.role.key.error}")
    private String roleKey;

    /**
     * 角色排序
     */
    @OrderBy(asc = true)
    @ExcelProperty(value = "角色排序")
    @NotNull(message = "{system.role.order.notnull}")
    private Integer roleSort;

    /**
     * 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限）
     */
    @ExcelProperty(value = "数据范围", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "1=所有数据权限,2=自定义数据权限,3=本部门数据权限,4=本部门及以下数据权限,5=仅本人数据权限")
    private String dataScope;

    /**
     * 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示）
     */
    @TableField(typeHandler = BooleanTypeHandler.class)
    private Boolean menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ）
     */
    @TableField(typeHandler = BooleanTypeHandler.class)
    private Boolean deptCheckStrictly;

    /**
     * 角色状态（0正常 1停用）
     */
    @ExcelProperty(value = "角色状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 用户是否存在此角色标识 默认不存在
     */
    @TableField(exist = false)
    private Boolean flag;

    /**
     * 菜单组
     */
    @TableField(exist = false)
    private List<Long> menuIds;

    /**
     * 部门组（数据权限）
     */
    @TableField(exist = false)
    private List<Long> deptIds;

    private String remark;

    public SysRoleEntity(Long id) {
        this.setId(id);
    }

    public static boolean isAdmin(Long roleId) {
        return roleId != null && 1L == roleId;
    }

    @JsonIgnore
    public boolean isAdmin() {
        return isAdmin(this.getId());
    }


    public void addInit() {
        this.delFlag = SystemConstants.NORMAL_STATUS;
        // 仅本人数据
        this.dataScope = "5";
        this.roleKey = "role" + ShortIdUtils.genId(5);
    }

}
