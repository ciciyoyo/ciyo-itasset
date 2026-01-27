package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.itam.entity.StocktakesEntity;
import com.ciyocloud.itam.req.StocktakesPageReq;
import com.ciyocloud.itam.vo.StocktakesDetailVO;
import com.ciyocloud.itam.vo.StocktakesVO;

import java.util.List;

/**
 * 盘点任务Service接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface StocktakesService extends IService<StocktakesEntity> {


    /**
     * 分页查询盘点任务VO
     */
    Page<StocktakesVO> queryPageVo(Page<StocktakesEntity> page, StocktakesPageReq req);

    /**
     * 列表查询
     */
    List<StocktakesVO> queryListVo(StocktakesPageReq req);

    /**
     * 新增盘点任务
     *
     * @param stocktakes 盘点任务
     * @return 结果
     */
    Boolean add(StocktakesEntity stocktakes);

    /**
     * 获取资产处于正在盘点中的任务
     *
     * @param assetId 资产ID
     * @return 盘点任务列表
     */
    List<StocktakesVO> getProcessingStocktakesByAssetId(Long assetId);

    /**
     * 获取盘点任务详情
     *
     * @param id 主键
     * @return 结果
     */
    StocktakesDetailVO getDetailVo(Long id);
}
