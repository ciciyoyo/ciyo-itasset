package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.itam.entity.DeviceEntity;
import com.ciyocloud.itam.req.DevicePageReq;
import com.ciyocloud.itam.vo.DeviceVO;

import java.util.List;
import java.util.Map;

/**
 * 设备管理Service接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface DeviceService extends IService<DeviceEntity> {

    /**
     * 分页查询设备列表 (VO)
     *
     * @param page 分页参数
     * @param req  查询条件
     * @return 分页结果
     */
    Page<DeviceVO> queryPageVo(Page<DeviceEntity> page, DevicePageReq req);

    /**
     * 查询设备列表 (VO) 不分页
     *
     * @param req 查询条件
     * @return 结果
     */
    List<DeviceVO> queryListVo(DevicePageReq req);

    /**
     * 设备报废
     *
     * @param ids ID列表
     * @return 结果
     */
    Boolean scrap(List<Long> ids);

    /**
     * 批量删除设备
     *
     * @param ids ID列表
     * @return 结果
     */
    boolean removeDevicesByIds(List<Long> ids);


    /**
     * 获取设备汇总统计数据
     *
     * @return 统计结果
     */
    Map<String, Object> getSummaryStats();




}

