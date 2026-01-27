package com.ciyocloud.itam.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.itam.service.DeviceDetailService;
import com.ciyocloud.itam.service.StocktakesService;
import com.ciyocloud.itam.vo.DeviceScanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备扫码查看外部接口
 *
 * @author codeck
 * @since 2026-01-27
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/device/scan")
public class DeviceScanController {

    private final DeviceDetailService deviceDetailService;
    private final StocktakesService stocktakesService;

    /**
     * 根据设备编号查询设备详情
     *
     * @param deviceNo 设备编号
     */
    @GetMapping(value = "/detail/{deviceNo}")
    @SaCheckPermission("itam:device:scan")
    public Result<DeviceScanVO> getDetailByNo(@PathVariable String deviceNo) {
        return Result.success(deviceDetailService.getDeviceDetailByNo(deviceNo));
    }


}
