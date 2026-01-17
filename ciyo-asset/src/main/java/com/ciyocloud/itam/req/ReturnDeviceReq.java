package com.ciyocloud.itam.req;

import lombok.Data;

/**
 * 设备归还申请请求对象
 *
 * @author codeck
 * @since 2026-01-17
 */
@Data
public class ReturnDeviceReq {

    private Long deviceId;

    private String returnDate;

    private String reason;
}
