package com.ciyocloud.system.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.OrderBy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.system.constant.SystemConstants;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门表 sys_dept
 *
 * @author codeck
 */
@Data
@TableName("sys_dept")
@FieldNameConstants
public class SysDeptEntity extends SysBaseEntity {


    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 祖级列表
     */
    private String ancestors;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 显示顺序
     */
    @OrderBy(asc = true)
    private Integer orderNum;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 部门状态:0正常,1停用
     */
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 父部门名称
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 子部门
     */
    @TableField(exist = false)
    private List<SysDeptEntity> children = new ArrayList<>();


    public void addInit() {
        this.delFlag = SystemConstants.NORMAL_STATUS;
        if (null == this.getParentId()) {
            this.setParentId(0L);
        }
        if (StrUtil.isBlank(this.getStatus())) {
            this.setStatus(SystemConstants.NORMAL_STATUS);
        }
        if (StrUtil.isBlank(this.getDelFlag())) {
            this.setDelFlag(SystemConstants.NORMAL_STATUS);
        }
    }

}
