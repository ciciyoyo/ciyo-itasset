package com.ciyocloud.message.websocket;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author : codeck
 * @description :
 * @create :  2021/12/31 11:28
 **/
@Data
@Builder
public class WsMessage {

    public MsgType msgType;


    private Object body;

    @AllArgsConstructor
    public enum MsgType {
        SYS_MSG("sysMsgNotice", " 系统消息");

        @JsonValue
        private String value;
        private String desc;
    }

}
