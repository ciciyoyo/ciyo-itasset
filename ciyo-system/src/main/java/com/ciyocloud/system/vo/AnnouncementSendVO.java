package com.ciyocloud.system.vo;

import com.ciyocloud.system.entity.SysAnnounceEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @Description: 用户通告阅读标记表
 * @Author: jeecg-boot
 * @since: 2019-02-21
 * @Version: V1.0
 */
@Data
public class AnnouncementSendVO {

    /**
     * id
     */
    private Long id;
    /**
     * 通告id
     */
    private Long anntId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String msgContent;
    /**
     * 发布人
     */
    private String sender;
    /**
     * 优先级（L低，M中，H高）
     */
    private SysAnnounceEntity.PriorityEnum priority;
    /**
     * 阅读状态
     */
    private Boolean readFlag;
    /**
     * 发布时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
    /**
     * 消息类型1:通知公告2:系统消息
     */
    private SysAnnounceEntity.MsgCategoryEnum msgCategory;
    /**
     * 业务id
     */
    private String busId;
    /**
     * 业务类型
     */
    private String busType;
    /**
     * 打开方式 组件：component 路由：url
     */
    private String openType;
    /**
     * 组件/路由 地址
     */
    private String openPage;

    /**
     * 业务类型查询（0.非bpm业务）
     */
    private String bizSource;

    /**
     * 摘要
     */
    private String msgAbstract;

}
