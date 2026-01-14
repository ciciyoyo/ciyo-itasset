package com.ciyocloud.message.handle.impl;

import com.ciyocloud.message.entity.MsgSendResult;
import com.ciyocloud.message.handle.SendMsgHandle;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class WxSendMsgHandle implements SendMsgHandle {


    /**
     * 处理默认的收到消息通知模版
     */
//    public static void handleSpTemplate(WxMpTemplateMessage templateMessage, Map<String, Object> params, String content) {
    // 自动填充自动值 不然会报错 https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Template_Message_Interface.html
//        if (ObjectUtil.isNotNull(params.get("remark"))) {
//            // 正则提取提取全部{}里面得值
//            String regex = "\\$\\{([^}]+)\\}";
//            Pattern pattern = Pattern.compile(regex);
//            Matcher matcher = pattern.matcher(content);
//            int count = 1;
//            while (matcher.find()) {
//                String group = matcher.group(1);
//                if (StrUtil.isBlank(group)) {
//                    continue;
//                }
//                if (group.contains("thing")) {
//                    if (count == 1) {
//                        templateMessage.addData(new WxMpTemplateData(group, params.get("first").toString(), ""));
//                        count++;
//                    } else {
//                        templateMessage.addData(new WxMpTemplateData(group, "点击查看", ""));
//                    }
//                } else if (group.contains("character_string")) {
//                    templateMessage.addData(new WxMpTemplateData(group, "1", ""));
//                } else if (group.contains("time")) {
//                    templateMessage.addData(new WxMpTemplateData(group, DateUtil.date().toString("yyyy年MM月dd日 HH:mm"), ""));
//                }
//            }
//        }
//    }
    @Override
    public MsgSendResult sendMsg(String sentTo, String title, String content, String thirdTemplateId, Map<String, Object> params) {
//        WxMpService wxService = SpringContextUtils.getBean(WxMpService.class);
//        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder().
//                toUser(sentTo).templateId(thirdTemplateId).url(MapUtil.getStr(params, JUMP_URL_KEY)).build();
//        params.keySet().forEach(key -> {
//            templateMessage.addData(new WxMpTemplateData(key, params.get(key).toString(), ""));
//        });
//        handleSpTemplate(templateMessage, params, content);
//        try {
//            String templateId = wxService.getTemplateMsgService().sendTemplateMsg(templateMessage);
//            return new MsgSendResult(true, templateId);
//        } catch (WxErrorException e) {
//            log.error("发送微信模板消息错误", e);
//            return new MsgSendResult(false, e.getMessage());
//        }
        return null;
    }


}
