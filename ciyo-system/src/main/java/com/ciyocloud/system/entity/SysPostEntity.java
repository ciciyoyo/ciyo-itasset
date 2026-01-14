package com.ciyocloud.system.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.excel.annotation.ExcelDictFormat;
import com.ciyocloud.excel.convert.ExcelDictConvert;
import lombok.Data;

/**
 * 岗位表 sys_post
 *
 * @author codeck
 */
@Data
@ExcelIgnoreUnannotated
@TableName("sys_post")
public class SysPostEntity extends SysBaseEntity {

    /**
     * 岗位序号
     */
    @ExcelProperty(value = "岗位序号")
    private Long id;

    /**
     * 岗位编码
     */
    @ExcelProperty(value = "岗位编码")
    private String postCode;

    /**
     * 岗位名称
     */
    @ExcelProperty(value = "岗位名称")
    private String postName;

    /**
     * 岗位排序 没用到
     */
    @ExcelProperty(value = "岗位层级")
    private Integer level;

    /**
     * 领导岗位
     */
    private Integer leaderPost;
    /**
     * 状态（0正常 1停用）
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=正常,1=停用")
    private Integer status;

    /***
     * 备注
     */
    private String remark;

    public void addInit() {
        this.level = 0;
        if (null == this.status) {
            this.status = 0;
        }
    }
}
