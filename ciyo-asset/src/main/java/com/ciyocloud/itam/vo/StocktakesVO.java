package com.ciyocloud.itam.vo;

import com.ciyocloud.itam.entity.StocktakesEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 盘点任务VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StocktakesVO extends StocktakesEntity {
    /**
     * 存放位置名称
     */
    private String locationName;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 负责人名称
     */
    private String managerName;
}
