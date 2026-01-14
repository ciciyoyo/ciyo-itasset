package com.ciyocloud.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.message.entity.enums.MsgTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author : codeck
 * @description : 消息模板实体
 * @create :  2021/06/07 16:37
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_msg_template")
public class SysMsgTemplateEntity extends SysBaseEntity {
    /**
     * 模板CODE
     */
    private String templateCode;
    /**
     * 模板标题
     */
    private String templateName;
    /**
     * 模板内容
     */
    private String templateContent;
    /**
     * 扩展字段  如果是短信时用来存储短信模板id
     */
    private String thirdTemplateId;
    /**
     * 模板类型
     */
    private MsgTypeEnum templateType;
}
