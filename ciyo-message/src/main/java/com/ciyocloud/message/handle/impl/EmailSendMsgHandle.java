package com.ciyocloud.message.handle.impl;

import com.ciyocloud.message.entity.MsgSendResult;
import com.ciyocloud.message.handle.SendMsgHandle;
import com.ciyocloud.message.thirdparty.email.MailService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


@Slf4j
public class EmailSendMsgHandle implements SendMsgHandle {


    @Override
    public MsgSendResult sendMsg(String sentTo, String title, String content, String thirdTemplateId, Map<String, Object> params) {
        try {
            MailService.sendHtmlMail(sentTo, title, content);
        } catch (MessagingException e) {
            log.error("发送邮件错误", e);
        }
        return new MsgSendResult(true, null);
    }
}

