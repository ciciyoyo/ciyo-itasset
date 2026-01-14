package com.ciyocloud.itam.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 耗材出入库明细对象 itam_consumable_transactions
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_consumable_transactions")
public class ConsumableTransactionsEntity extends SysBaseEntity {

    /**
     * 主键 ID
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 关联耗材 ID
     */
    @NotNull(message = "关联耗材 ID不能为空")
    private Long consumableId;
    /**
     * 操作人 ID
     */
    private Long operatorId;
    /**
     * 变动类型
     */
    @NotBlank(message = "变动类型不能为空")
    private String actionType;
    /**
     * 变动数量
     */
    @NotNull(message = "变动数量不能为空")
    private Long quantity;
    /**
     * 变动后结存
     */
    @NotNull(message = "变动后结存不能为空")
    private Long remainingQuantity;
    /**
     * 关联对象类型
     */
    private String targetType;
    /**
     * 关联对象 ID
     */
    private Long targetId;
    /**
     * 备注
     */
    private String note;

}
