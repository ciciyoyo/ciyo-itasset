package com.ciyocloud.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 发送站内信事件
 *
 * @author codeck
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendInternalMsgEvent implements Serializable {

    /**
     * 标题
     */
    private String title;
    /**
     * 消息内容
     */
    private String msgContent;
    /**
     * 用户Id
     */
    private Long userId;
    /**
     * 来源Id
     */
    private String sourceId;
    /**
     * 打开链接
     */
    private String openUrl;


}
