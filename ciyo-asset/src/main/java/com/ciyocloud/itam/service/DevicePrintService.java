package com.ciyocloud.itam.service;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 设备打印Service接口
 *
 * @author codeck
 */
public interface DevicePrintService {

    /**
     * 批量打印设备标签
     *
     * @param ids      设备ID列表
     * @param response 响应对象
     */
    void printLabels(List<Long> ids, HttpServletResponse response);
}
