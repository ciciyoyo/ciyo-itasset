package com.ciyocloud.common.redis.receiver;


import cn.hutool.core.util.ObjectUtil;
import com.ciyocloud.common.redis.listener.RedisMessage;
import com.ciyocloud.common.redis.listener.RedisMessageLister;
import com.ciyocloud.common.util.SpringContextUtils;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author codeck
 */
@Data
@Component
public class RedisReceiver {


    /**
     * 接受消息并调用业务逻辑处理器
     *
     * @param message 消息
     */
    public void onMessage(RedisMessage message) {
        Object handlerName = message.getHandlerName();
        RedisMessageLister messageListener = SpringContextUtils.getBean(handlerName.toString());
        if (ObjectUtil.isNotEmpty(messageListener)) {
            messageListener.onMessage(message);
        }
    }

}
