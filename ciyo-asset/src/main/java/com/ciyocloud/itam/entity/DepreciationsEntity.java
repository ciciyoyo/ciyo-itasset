package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.common.mybatis.handler.JacksonTypeHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 折旧规则对象 itam_depreciations
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_depreciations")
@ExcelIgnoreUnannotated
public class DepreciationsEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId(value = "id")
    @ExcelProperty(value = "折旧规则ID")
    private Long id;
    /**
     * 折旧规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    @ExcelProperty(value = "折旧规则名称")
    private String name;
    /**
     * 1启用 0停用
     */
    @ExcelProperty(value = "状态")
    private Long status;
    /**
     * 总折旧月数（展示/校验/索引）
     */
    @NotNull(message = "折旧周期不能为空")
    @ExcelProperty(value = "总折旧月数")
    private Long months;
    /**
     * amount / percent
     */
    @ExcelProperty(value = "最低残值类型")
    private String floorType;
    /**
     * 最低残值
     */
    @ExcelProperty(value = "最低残值")
    private BigDecimal floorVal;
    /**
     * 分阶段折旧规则（核心）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<RulePeriod> stages;

    /**
     * 描述
     */
    @ExcelProperty(value = "描述")
    private String remark;


    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;


    /**
     * 周期单位
     */
    @Getter
    @AllArgsConstructor
    public static enum PeriodUnit {

        DAY("天"), MONTH("月"), YEAR("年");

        private final String label;

    }

    @Data
    public static class RulePeriod implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 周期数值，如 12
         */
        private Integer period;

        /**
         * 周期单位（DAY / MONTH / YEAR）
         */
        private PeriodUnit unit;

        /**
         * 比例，0 ~ 1
         */
        private BigDecimal ratio;

    }

}
