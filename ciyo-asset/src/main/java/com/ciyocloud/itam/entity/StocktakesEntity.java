package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.excel.convert.DictEnumConvert;
import com.ciyocloud.itam.enums.StocktakeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


/**
 * 盘点任务对象 itam_stocktakes
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_stocktakes")
@ExcelIgnoreUnannotated
public class StocktakesEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "盘点任务ID")
    private Long id;
    /**
     * 盘点任务名称
     */
    @NotBlank(message = "盘点任务名称不能为空")
    @ExcelProperty(value = "盘点任务名称")
    private String name;
    /**
     * 盘点位置范围 ID
     */
    @ExcelProperty(value = "盘点位置范围ID")
    private Long locationId;
    /**
     * 盘点分类范围 ID
     */
    @ExcelProperty(value = "盘点分类范围ID")
    private Long categoryId;
    /**
     * 状态
     */
    @ExcelProperty(value = "状态", converter = DictEnumConvert.class)
    private StocktakeStatus status;
    /**
     * 负责人 ID
     */
    @NotNull(message = "负责人 ID不能为空")
    @ExcelProperty(value = "负责人ID")
    private Long managerId;
    /**
     * 计划开始日期
     */
    @ExcelProperty(value = "计划开始日期")
    private LocalDateTime startDate;
    /**
     * 计划/实际结束日期
     */
    @ExcelProperty(value = "计划/实际结束日期")
    private LocalDateTime endDate;
    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String note;

}
