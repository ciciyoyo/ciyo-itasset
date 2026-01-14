package com.ciyocloud.common.redis.receiver;


import cn.hutool.core.util.ObjectUtil;
import com.ciyocloud.common.constant.RedisKeyConstants;
import com.ciyocloud.common.redis.listener.TduckRedisLister;
import com.ciyocloud.common.util.SpringContextUtils;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author codeck
 */
@Data
@Component
public class RedisReceiver {


    /**
     * 接受消息并调用业务逻辑处理器
     *
     * @param params
     */
    public void onMessage(Map<String, String> params) {
        Object handlerName = params.get(RedisKeyConstants.HANDLER_NAME);
        TduckRedisLister messageListener = SpringContextUtils.getBean(handlerName.toString());
        if (ObjectUtil.isNotEmpty(messageListener)) {
            messageListener.onMessage(params);
        }
    }

}
