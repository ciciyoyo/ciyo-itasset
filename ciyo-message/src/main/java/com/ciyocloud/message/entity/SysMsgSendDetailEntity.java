package com.ciyocloud.message.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.BaseEntity;
import com.ciyocloud.message.entity.enums.MsgTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发送操作记录对象 sys_msg_send_detail
 *
 * @author codeck
 * @since 2023-12-23 21:11:06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_msg_send_detail")
public class SysMsgSendDetailEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 来源类型
     */
    private String sourceType;
    /**
     * 来源Id
     */
    private String sourceId;
    /**
     * 消息iD
     */
    private Long templateId;
    /**
     * 发送条数
     */
    private Long sendCount;
    /**
     * 消息类型
     */
    private MsgTypeEnum msgType;
    /**
     * 消息内容
     */
    private String msgContent;

}
