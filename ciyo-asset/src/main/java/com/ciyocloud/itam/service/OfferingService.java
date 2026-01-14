package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.itam.entity.OfferingEntity;
import com.ciyocloud.itam.req.OfferingPageReq;
import com.ciyocloud.itam.vo.OfferingVO;

import java.util.List;
import java.util.Map;

/**
 * 服务Service接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface OfferingService extends IService<OfferingEntity> {

    /**
     * 查询服务列表 VO
     */
    Page<OfferingVO> queryPageVo(
            Page<OfferingVO> page,
            OfferingPageReq req);

    /**
     * 查询服务列表 VO (不分页)
     */
    List<OfferingVO> queryListVo(OfferingPageReq req);

    /**
     * 获取服务详细信息 VO
     *
     * @param id 服务ID
     * @return OfferingVO
     */
    OfferingVO getOfferingDetail(Long id);

    /**
     * 服务归属到设备
     *
     * @param offeringId 服务ID
     * @param assetId    设备ID
     */
    void bindAsset(Long offeringId, Long assetId);

    /**
     * 解除服务归属
     *
     * @param offeringId 服务ID
     */
    void unbind(Long offeringId);


    /**
     * 获取服务常用指标统计
     *
     * @return 统计结果
     */
    Map<String, Object> getOfferingStatistics();

    /**
     * 批量删除服务
     *
     * @param ids ID列表
     * @return 结果
     */
    boolean removeOfferingsByIds(List<Long> ids);


}
