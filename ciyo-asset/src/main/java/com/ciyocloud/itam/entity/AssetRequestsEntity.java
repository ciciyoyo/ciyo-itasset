package com.ciyocloud.itam.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.excel.annotation.ExcelSample;
import com.ciyocloud.excel.convert.DictEnumConvert;
import com.ciyocloud.excel.core.EnumSampleProvider;
import com.ciyocloud.itam.enums.AssetRequestStatus;
import com.ciyocloud.itam.enums.AssetType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 资产申请表 itam_asset_requests
 *
 * @author codeck
 * @since 2026-01-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_asset_requests")
@ExcelIgnoreUnannotated
public class AssetRequestsEntity extends SysBaseEntity {


    /**
     * 主键 ID
     */
    @TableId(value = "id")
    @ExcelProperty(value = "申请ID")
    private Long id;

    /**
     * 申请单号
     */
    @ExcelProperty(value = "申请单号")
    private String requestNo;

    /**
     * 申请人 ID
     */
    @NotNull(message = "申请人不能为空")
    @ExcelProperty(value = "申请人ID")
    private Long userId;

    /**
     * 资产类型 (device, license, accessory)
     */
    @NotNull(message = "资产类型不能为空")
    @ExcelProperty(value = "资产类型", converter = DictEnumConvert.class)
    @ExcelSample(provider = EnumSampleProvider.class)
    private AssetType itemType;

    /**
     * 申请分类 ID (可选, 譬如申请 "笔记本电脑" 类别)
     */
    @ExcelProperty(value = "申请分类ID")
    private Long categoryId;

    /**
     * 具体资产 ID (可选, 如果申请特定资产)
     */
    @ExcelProperty(value = "具体资产ID")
    private Long itemId;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @ExcelProperty(value = "数量")
    private Integer quantity;

    /**
     * 申请原因
     */
    @ExcelProperty(value = "申请原因")
    private String reason;

    /**
     * 是否长期使用
     */
    @ExcelProperty(value = "是否长期使用")
    private Boolean isLongTerm;

    /**
     * 预计归还时间
     */
    @ExcelProperty(value = "预计归还时间")
    private LocalDateTime expectedReturnTime;

    /**
     * 状态 (pending, approved, rejected, canceled)
     */
    @ExcelProperty(value = "状态", converter = DictEnumConvert.class)
    @ExcelSample(provider = EnumSampleProvider.class)
    private AssetRequestStatus status;

    /**
     * 审批人 ID
     */
    @ExcelProperty(value = "审批人ID")
    private Long approverId;

    /**
     * 审批时间
     */
    @ExcelProperty(value = "审批时间")
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    @ExcelProperty(value = "审批意见")
    private String approvalNote;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
