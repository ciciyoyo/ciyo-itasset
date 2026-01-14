package com.ciyocloud.message.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author : codeck
 * @description : 消息发送结果
 * @create :  2021/12/29 11:45
 **/
@Data
@AllArgsConstructor
public class MsgSendResult {
    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 返回结果
     */
    private String result;
}
