package com.ciyocloud.message.entity;

import com.ciyocloud.message.entity.enums.MsgTypeEnum;
import lombok.Data;

import java.util.Map;


/**
 * 发送消息
 */
@Data
public class SendMsgParams {

    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 消息类型
     */
    private MsgTypeEnum msgType;
    /**
     * 参数
     */
    private Map<String, Object> params;
    /**
     * 消息接收方
     */
    private String sentTo;

    /**
     * 模板id
     */
    private String templateId;
}
