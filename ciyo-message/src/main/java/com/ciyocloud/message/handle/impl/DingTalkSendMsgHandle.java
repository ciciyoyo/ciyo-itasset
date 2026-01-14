//package com.ciyocloud.message.handle.impl;
//
//import cn.hutool.core.map.MapUtil;
//import cn.hutool.core.util.StrUtil;
//import com.dingtalk.api.DefaultDingTalkClient;
//import com.dingtalk.api.DingTalkClient;
//import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
//import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
//import com.taobao.api.ApiException;
//import com.ciyocloud.integration.config.DingTalkConfiguration;
//import com.ciyocloud.message.entity.MsgSendResult;
//import com.ciyocloud.message.handle.SendMsgHandle;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.Map;
//
//import static com.ciyocloud.common.constant.CommonConstants.JUMP_URL_KEY;
//
/// **
// * 钉钉应用消息发送处理器
// *
// * @author codeck
// */
//@Slf4j
//public class DingTalkSendMsgHandle implements SendMsgHandle {
//
//    @Override
//    public MsgSendResult sendMsg(String sentTo, String title, String content, String thirdTemplateId,
//                                 Map<String, Object> params) {
//        try {
//            String accessToken = DingTalkConfiguration.getAccessToken();
//            if (StrUtil.isBlank(accessToken)) {
//                log.error("获取钉钉AccessToken失败");
//                return new MsgSendResult(false, "获取钉钉AccessToken失败");
//            }
//            // 构建消息内容
//            OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
//            msg.setMsgtype("oa");
//
//            // 构建OA消息体
//            OapiMessageCorpconversationAsyncsendV2Request.OA oa = new OapiMessageCorpconversationAsyncsendV2Request.OA();
//            oa.setHead(new OapiMessageCorpconversationAsyncsendV2Request.Head());
//            oa.getHead().setText(title);
//
//            oa.setBody(new OapiMessageCorpconversationAsyncsendV2Request.Body());
//            oa.getBody().setContent(content);
//
//            // 设置跳转链接
//            String jumpUrl = MapUtil.getStr(params, JUMP_URL_KEY);
//            if (StrUtil.isNotBlank(jumpUrl)) {
//                oa.setMessageUrl(jumpUrl);
//                oa.setPcMessageUrl(jumpUrl);
//            }
//
//            msg.setOa(oa);
//
//            // 创建请求
//            DingTalkClient client = new DefaultDingTalkClient(
//                    "https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
//            OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
//            request.setAgentId(DingTalkConfiguration.getDingTalkProperties().getAgentId());
//            request.setUseridList(sentTo);
//            request.setMsg(msg);
//            request.setToAllUser(false);
//
//            // 发送消息
//            OapiMessageCorpconversationAsyncsendV2Response response = client.execute(request, accessToken);
//
//            if (response.isSuccess()) {
//                Long taskId = response.getTaskId();
//                return new MsgSendResult(true, String.valueOf(taskId));
//            } else {
//                String errorMsg = String.format("钉钉消息发送失败: %s - %s", response.getErrorCode(), response.getErrmsg());
//                log.error(errorMsg);
//                return new MsgSendResult(false, errorMsg);
//            }
//        } catch (ApiException e) {
//            log.error("发送钉钉应用消息错误", e);
//            return new MsgSendResult(false, "发送钉钉应用消息错误: " + e.getMessage());
//        } catch (Exception e) {
//            log.error("发送钉钉应用消息异常", e);
//            return new MsgSendResult(false, "发送钉钉应用消息异常: " + e.getMessage());
//        }
//    }
//}
