package com.ciyocloud.system.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.common.jackson.LongToStringSerializer;
import com.ciyocloud.common.mybatis.handler.JacksonTypeHandler;
import com.ciyocloud.excel.annotation.ExcelDictFormat;
import com.ciyocloud.excel.annotation.ExcelPropertyType;
import com.ciyocloud.excel.annotation.ExcelTemplate;
import com.ciyocloud.excel.convert.ExcelDictConvert;
import com.ciyocloud.excel.convert.ExcelLongListConvert;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户对象 sys_user
 *
 * @author codeck
 */
@Data
@ExcelIgnoreUnannotated
@TableName(value = "sys_user", autoResultMap = true)
@ExcelTemplate(code = "sysUser", sheetName = "用户导入模板")
public class SysUserEntity extends SysBaseEntity {

    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户序号")
    @JsonSerialize(using = LongToStringSerializer.class)
    private Long id;

    /**
     * 部门ID
     */
    @ExcelProperty(value = "部门编号")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.IMPORT)
    private Long deptId;

    /**
     * 用户账号
     */
    @ExcelProperty(value = "登录名称")
    @NotBlank(message = "{system.user.name.notnull}")
    @Size(min = 0, max = 30, message = "{system.user.name.error}")
    private String userName;

    /**
     * 用户昵称
     */
    @ExcelProperty(value = "用户名称")
    @Size(min = 0, max = 30, message = "{system.user.nickname.error}")
    private String nickName;

    /**
     * 用户邮箱
     */
    @ExcelProperty(value = "用户邮箱")
    @Email(message = "{system.user.email.error}")
    @Size(min = 0, max = 50, message = "{system.user.email.lenError}")
    private String email;

    /**
     * 手机号码
     */
    @ExcelProperty(value = "手机号码")
    @Size(min = 0, max = 11, message = "{system.user.phone.lenError}")
    private String phonenumber;

    /**
     * 用户性别
     */
    @ExcelProperty(value = "用户性别", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 密码
     */
    @ExcelProperty(value = "密码")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.IMPORT)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * 盐加密
     */
    @TableField(exist = false)
    private String salt;

    /**
     * 帐号状态（0正常 1停用）
     */
    @ExcelProperty(value = "帐号状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 最后登录IP
     */
    @ExcelProperty(value = "最后登录IP")
    private String loginIp;

    /**
     * 最后登录时间
     */
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginDate;


    /**
     * 部门对象
     */
    @TableField(exist = false)
    private SysDeptEntity dept;

    /**
     * 角色对象
     */
    @TableField(exist = false)
    private List<SysRoleEntity> roles;

    /**
     * 角色组
     */
    @TableField(exist = false)
    @ExcelProperty(value = "角色Id", converter = ExcelLongListConvert.class)
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.IMPORT)
    private List<Long> roleIds;

    /**
     * 岗位组
     */
    @TableField(exist = false)
    @ExcelProperty(value = "部门岗位Id", converter = ExcelLongListConvert.class)
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.IMPORT)
    private List<Long> postIds;
    /**
     * 备注
     */
    private String remark;


    @TableField(typeHandler = JacksonTypeHandler.class)
    private ExtraInfo extraInfo;

    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    @JsonIgnore
    public boolean isAdmin() {
        return isAdmin(this.id);
    }

    @JsonIgnore
    @JsonProperty
    public String getPassword() {
        return password;
    }

    @Data
    @FieldNameConstants
    public static class ExtraInfo {
        /**
         * 是否修改过账号名称
         */
        private Boolean modifyAccount;
        /**
         * 是否修改过面膜
         */
        private Boolean changedPassword;
    }
}
