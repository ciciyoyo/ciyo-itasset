package com.ciyocloud.itam.vo;

import com.ciyocloud.itam.entity.ConsumableTransactionsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 耗材出入库明细VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumableTransactionsVO extends ConsumableTransactionsEntity {
    /**
     * 耗材名称
     */
    private String consumableName;
    /**
     * 操作人名称
     */
    private String operatorName;
    /**
     * 关联对象名称
     */
    private String targetName;
}
