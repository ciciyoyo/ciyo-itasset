package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.mybatis.service.BaseService;
import com.ciyocloud.itam.entity.OfferingEntity;
import com.ciyocloud.itam.req.OfferingPageReq;
import com.ciyocloud.itam.vo.OfferingVO;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 服务Service接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface OfferingService extends BaseService<OfferingEntity> {

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

    /**
     * 导入服务数据
     *
     * @param inputStream      文件输入流
     * @param originalFilename 原始文件名
     * @param progressKey      前端传递的进度监听key
     * @param userId           用户 id
     */
    void importData(InputStream inputStream, String originalFilename, String progressKey, Long userId);

}
