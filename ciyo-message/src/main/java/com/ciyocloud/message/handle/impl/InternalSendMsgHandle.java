package com.ciyocloud.message.handle.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import com.ciyocloud.common.event.SendInternalMsgEvent;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.message.entity.MsgSendResult;
import com.ciyocloud.message.handle.SendMsgHandle;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class InternalSendMsgHandle implements SendMsgHandle {


    @Override
    public MsgSendResult sendMsg(String sentTo, String title, String content, String thirdTemplateId, Map<String, Object> params) {
        try {
            SpringContextUtils.publishEvent(new SendInternalMsgEvent(title, content, Convert.toLong(sentTo),
                    MapUtil.getStr(params, "sourceId"), MapUtil.getStr(params, "openUrl")));
            return new MsgSendResult(true, null);
        } catch (Exception e) {
            log.error("发送站内信错误", e);
            return new MsgSendResult(false, e.getMessage());
        }
    }
}
