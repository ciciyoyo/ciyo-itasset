package com.ciyocloud.itam.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 型号对象 itam_models
 *
 * @author codeck
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_models")
public class ModelsEntity extends SysBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 型号名称
     */
    @NotBlank(message = "型号名称不能为空")
    private String name;

    /**
     * 型号图片
     */
    private String imageUrl;

    /**
     * 厂商 ID
     */
    @NotNull(message = "厂商 ID不能为空")
    private Long manufacturerId;

    /**
     * 分类 ID
     */
    @NotNull(message = "分类 ID不能为空")
    private Long categoryId;

    /**
     * 关联折旧规则 ID
     */
    private Long depreciationId;

    /**
     * 型号编码
     */
    private String modelNumber;

    /**
     * 报废年限 (月)
     */
    private Integer eol;


    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
