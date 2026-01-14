package com.ciyocloud.message.entity.enums;

import com.ciyocloud.common.entity.IDictEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型
 *
 * @author codeck
 */
@Getter
@AllArgsConstructor
public enum MsgTypeEnum implements IDictEnum {
    SMS(1, "短信"),
    EMAIL(2, "邮件"),
    WX_MP(3, "微信公众号"),
    WX_CP(5, "企业微信"),
    INTERNAL(4, "站内信"),
    CHAT_HOOK(6, "群机器人"),
    DING_TALK(7, "钉钉"),
    LARK(8, "飞书");

    private Integer value;

    private String desc;
}
