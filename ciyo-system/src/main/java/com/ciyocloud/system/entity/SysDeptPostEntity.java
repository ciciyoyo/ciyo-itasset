package com.ciyocloud.system.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.BaseEntity;
import lombok.Data;

/**
 * 部门岗位关系对象 sys_dept_post
 *
 * @author codeck-gen
 * @since 2022-06-06 16:25:10
 */
@Data
@ExcelIgnoreUnannotated
@TableName(value = "sys_dept_post")
public class SysDeptPostEntity extends BaseEntity {

    /**
     * 主键Id
     */
    private Long id;

    /**
     * 部门Id
     */
    @ExcelProperty(value = "部门Id")
    private Long deptId;

    /**
     * 岗位Id
     */
    @ExcelProperty(value = "岗位Id")
    private Long postId;

    /**
     * 部门岗位显示名称
     */
    @ExcelProperty(value = "部门岗位显示名称")
    private String postShowName;


}
