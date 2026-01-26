package com.ciyocloud.itam.service;

import com.ciyocloud.itam.vo.DeviceDetailVO;
import com.ciyocloud.itam.vo.DeviceScanVO;

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
    /**
     * 根据设备编号获取设备详情（包含盘点信息）
     *
     * @param deviceNo 设备编号
     * @return 设备扫码详情
     */
    DeviceScanVO getDeviceDetailByNo(String deviceNo);
}
