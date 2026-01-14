package com.ciyocloud.message.handle;

import com.ciyocloud.message.entity.MsgSendResult;

import java.util.Map;

public interface SendMsgHandle {


    /**
     * 发送消息
     *
     * @param sentTo          接收人
     * @param title           标题
     * @param content         内容
     * @param thirdTemplateId 三方模板Id
     * @param params          动态参数
     */
    MsgSendResult sendMsg(String sentTo, String title, String content, String thirdTemplateId, Map<String, Object> params);
}
