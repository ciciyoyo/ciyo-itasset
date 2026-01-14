package com.ciyocloud.system.vo;

import com.ciyocloud.system.entity.SysAnnounceEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author : codeck
 * @description : 未读公告
 * @create :  2022/12/20 17:20
 **/
@Data
public class UnReadAnnouncementVO {

    private Long id;
    /**
     * 消息内容
     */
    private String msgContent;
    /**
     * 消息类型
     */
    private String msgType;
    /**
     * 发送人
     */
    private String sender;

    /**
     * 标题
     */
    private String title;
    /**
     * 类型
     */
    private String msgCategory;
    /**
     * 优先级
     */
    private SysAnnounceEntity.PriorityEnum priority;
    /**
     * 发送事件
     */
    private LocalDateTime sendTime;
}
