package com.ciyocloud.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @Description: 用户通告阅读标记表
 */
@Data
@TableName("sys_announcement_send")
public class SysAnnounceSendEntity extends SysBaseEntity {


    /**
     * 通告id
     */
    private Long anntId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 阅读状态（0未读，1已读）
     */
    private String readFlag;
    /**
     * 阅读时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;


}
