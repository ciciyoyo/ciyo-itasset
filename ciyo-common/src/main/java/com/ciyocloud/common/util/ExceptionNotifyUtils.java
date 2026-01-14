package com.ciyocloud.common.util;


import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 异常通知工具类
 * 当发生异常时 推送消息到企微
 *
 * @author codeck
 */
@Component
@Slf4j
public class ExceptionNotifyUtils {
    /**
     * 是否启用
     */
    @Value("${platform.exception.notify.enable:false}")
    private Boolean exceptionNotifyEnable = false;

    @Value("${platform.exception.notify.url:''}")
    private String exceptionNotifyUrl;


    /**
     * 发送消息
     *
     * @param exception 异常
     */
    public void notify(Exception exception) {
        if (null != exceptionNotifyEnable && exceptionNotifyEnable) {
            // 企微推送异常信息
            try {
                Map<String, Object> data = MapUtil.newHashMap();
                data.put("msgtype", "text");
                Map<String, Object> text = MapUtil.newHashMap();
                text.put("content", ExceptionUtil.stacktraceToString(exception));
                data.put("text", text);
                HttpUtil.post(exceptionNotifyUrl, JSONUtil.toJsonStr(data));
            } catch (Exception e) {
                log.error("推送错误信息失败", e);
            }
        }
    }


}
