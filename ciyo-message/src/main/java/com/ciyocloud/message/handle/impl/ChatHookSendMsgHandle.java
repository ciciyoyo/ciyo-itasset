package com.ciyocloud.message.handle.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import com.ciyocloud.common.util.JsonUtils;
import com.ciyocloud.message.entity.MsgSendResult;
import com.ciyocloud.message.handle.SendMsgHandle;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


@Slf4j
public class ChatHookSendMsgHandle implements SendMsgHandle {


    @Override
    public MsgSendResult sendMsg(String sentTo, String title, String content, String thirdTemplateId, Map<String, Object> params) {
        try {
            sendWebHook(HtmlUtil.cleanHtmlTag(content), thirdTemplateId);
            return new MsgSendResult(true, null);
        } catch (Exception e) {
            log.error("发送邮件错误", e);
            return new MsgSendResult(false, e.getMessage());
        }
    }


    /**
     * 发送webhook
     */
    public void sendWebHook(String content, String thirdTemplateId) {
        if (thirdTemplateId.contains("weixin.qq.com")) {
            pushWxHook(content, thirdTemplateId);
        } else if (thirdTemplateId.contains("dingtalk.com")) {
            pushDingDingHook(content, thirdTemplateId);
        } else if (thirdTemplateId.contains("feishu")) {
            pushFeiShuDingHook(content, thirdTemplateId);
        }

    }

    public void pushWxHook(String content, String thirdTemplateId) {
        Map<String, Object> data = MapUtil.of();
        data.put("msgtype", "text");
        Map<Object, Object> text = MapUtil.of();
        text.put("content", content);
        data.put("text", text);
        String result = HttpUtil.post(thirdTemplateId.trim(), JsonUtils.objToJson(data));
        log.debug("发送微信hook结果:{}", result);
    }

    public void pushDingDingHook(String content, String thirdTemplateId) {
        Map<String, Object> data = MapUtil.of();
        data.put("msgtype", "text");
        Map<Object, Object> text = MapUtil.of();
        text.put("content", content);
        data.put("text", text);
        String result = HttpUtil.post(thirdTemplateId.trim(), JsonUtils.objToJson(data));
        log.debug("发送钉钉hook结果:{}", result);
    }

    public void pushFeiShuDingHook(String content, String thirdTemplateId) {
        Map<String, Object> data = MapUtil.of();
        data.put("msg_type", "text");
        Map<Object, Object> contentData = MapUtil.of();
        contentData.put("text", content);
        data.put("content", contentData);
        String result = HttpUtil.post(thirdTemplateId.trim(), JsonUtils.objToJson(data));
        log.debug("发送飞书hook结果:{}", result);
    }


}

