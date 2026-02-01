package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.mybatis.service.BaseService;
import com.ciyocloud.itam.entity.StocktakeItemsEntity;
import com.ciyocloud.itam.req.StocktakeItemsPageReq;
import com.ciyocloud.itam.vo.StocktakeItemsVO;

import java.util.List;

/**
 * 盘点明细Service接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface StocktakeItemsService extends BaseService<StocktakeItemsEntity> {

    /**
     * 分页查询
     */
    Page<StocktakeItemsVO> queryPageVo(Page<StocktakeItemsEntity> page, StocktakeItemsPageReq req);

    /**
     * 列表查询
     */
    List<StocktakeItemsVO> queryListVo(StocktakeItemsPageReq req);

    /**
     * 更新盘点明细
     *
     * @param stocktakeItems 盘点明细
     * @return 结果
     */
    Boolean updateStocktakeItem(StocktakeItemsEntity stocktakeItems);
}
