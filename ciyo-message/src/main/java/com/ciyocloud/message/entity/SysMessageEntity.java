package com.ciyocloud.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.IDictEnum;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.message.entity.enums.MsgTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author : codeck
 * @description : 消息实体类
 * @create :  2021/06/07 16:37
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_msg")
public class SysMessageEntity extends SysBaseEntity {


    private String msgId;

    /**
     * 推送内容
     */
    private String content;
    /**
     * 推送所需参数Json格式
     */
    private String msgParams;
    /**
     * 接收人
     */
    private String receiver;
    /**
     * 推送失败原因
     */
    private String result;
    /**
     * 发送次数
     */
    private Integer sendNum;
    /**
     * 推送状态 0未推送 1推送成功 2推送失败
     */
    private SendStatus sendStatus;
    /**
     * 推送时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
    /**
     * 消息标题
     */
    private String title;
    /**
     * 推送方式：1短信 2邮件 3微信
     */
    private MsgTypeEnum type;
    /**
     * 备注
     */
    private String remark;


    /**
     * 发送详情Id
     */
    private Long sendDetailId;

    /**
     * 扩展状态
     */
    private Integer extStatus;


    @AllArgsConstructor
    @Getter
    public enum SendStatus implements IDictEnum {
        NOT("0", "未推送"),
        SUCCESS("1", "推送成功"),
        FAIL("2", "推送失败");


        private String value;
        private String desc;
    }
}
