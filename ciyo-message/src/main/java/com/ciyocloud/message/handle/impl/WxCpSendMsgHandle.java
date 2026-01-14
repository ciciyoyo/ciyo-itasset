//package com.ciyocloud.message.handle.impl;
//
//import cn.hutool.core.map.MapUtil;
//import com.ciyocloud.message.entity.MsgSendResult;
//import com.ciyocloud.message.handle.SendMsgHandle;
//import com.ciyocloud.wx.cp.config.WxCpConfiguration;
//import lombok.extern.slf4j.Slf4j;
//import me.chanjar.weixin.common.error.WxErrorException;
//import me.chanjar.weixin.cp.bean.message.WxCpMessage;
//import me.chanjar.weixin.cp.bean.message.WxCpMessageSendResult;
//
//import java.util.Map;
//
//import static com.ciyocloud.common.constant.CommonConstants.JUMP_URL_KEY;
//
//@Slf4j
//public class WxCpSendMsgHandle implements SendMsgHandle {
//
//
//    @Override
//    public MsgSendResult sendMsg(String sentTo, String title, String content, String thirdTemplateId, Map<String, Object> params) {
//        WxCpMessage wxCpMessage = WxCpMessage
//                .TEXTCARD()
/// /                .agentId(...) // 企业号应用ID
//                .toUser(sentTo)
/// /                .toParty("非必填，PartyID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数")
/// /                .toTag("非必填，TagID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数")
//                .title(title)
//                .url(MapUtil.getStr(params, JUMP_URL_KEY, ""))
//                .description(content)
//                .btnTxt("详情")
//                .build();
//        try {
//            WxCpMessageSendResult send = WxCpConfiguration.getCpService().getMessageService().send(wxCpMessage);
//            return new MsgSendResult(true, send.toString());
//        } catch (WxErrorException e) {
//            log.error("发送企业微信模板消息错误", e);
//            return new MsgSendResult(false, e.getMessage());
//        }
//    }
//
//
//}
