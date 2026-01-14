package com.ciyocloud.message.event;


import lombok.Data;

/**
 * 修改消息扩展状态
 *
 * @author codeck
 */
@Data
public class UpdateMsgExtStatusEvent {

    /**
     * 消息Id
     */
    private String msgId;


    /**
     * 状态
     */
    private Integer status;

}
