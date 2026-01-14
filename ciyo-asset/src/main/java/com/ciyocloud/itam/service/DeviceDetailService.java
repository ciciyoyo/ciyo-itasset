package com.ciyocloud.itam.service;

import com.ciyocloud.itam.vo.DeviceDetailVO;

/**
 * 设备详情查询 Service 接口
 */
public interface DeviceDetailService {

    /**
     * 获取设备详情（包含关联资产）
     *
     * @param id 设备ID
     * @return 设备详情
     */
    DeviceDetailVO getDeviceDetail(Long id);
}
