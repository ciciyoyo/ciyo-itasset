package com.ciyocloud.message.handle.impl;

import com.ciyocloud.message.entity.MsgSendResult;
import com.ciyocloud.message.handle.SendMsgHandle;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


/**
 * 飞书应用消息发送处理器
 *
 * @author codeck
 */
@Slf4j
public class LarkSendMsgHandle implements SendMsgHandle {

    @Override
    public MsgSendResult sendMsg(String sentTo, String title, String content, String thirdTemplateId,
                                 Map<String, Object> params) {
//        try {
//            Client client = LarkConfiguration.getLarkClient();
//            if (client == null) {
//                log.error("飞书客户端为空");
//                return new MsgSendResult(false, "飞书客户端未初始化");
//            }
//            String jumpUrl = MapUtil.getStr(params, JUMP_URL_KEY);
//            String msgTemplate = "{\"schema\":\"2.0\",\"config\":{\"update_multi\":true},\"body\":{\"direction\":\"vertical\",\"elements\":[{\"tag\":\"markdown\",\"content\":\"%s\",\"text_size\":\"normal\",\"margin\":\"0px 0px 0px 0px\",\"element_id\":\"custom_id\"},{\"tag\":\"button\",\"text\":{\"tag\":\"plain_text\",\"content\":\"查看详情\"},\"type\":\"primary_filled\",\"width\":\"fill\",\"behaviors\":[{\"type\":\"open_url\",\"default_url\":\"%s\",\"pc_url\":\"\",\"ios_url\":\"\",\"android_url\":\"\"}],\"margin\":\"4px 0px 4px 0px\",\"element_id\":\"u_NEL9K7KwEnwfSdu_eY\"}]},\"header\":{\"title\":{\"tag\":\"plain_text\",\"content\":\"%s\"},\"subtitle\":{\"tag\":\"plain_text\",\"content\":\"\"},\"template\":\"blue\",\"icon\":{\"tag\":\"standard_icon\",\"token\":\"notification\"},\"padding\":\"12px 12px 12px 12px\"}}";
//            String msgContent = String.format(msgTemplate, content, jumpUrl, title);
//            // 创建请求对象
//            CreateMessageReq req = CreateMessageReq.newBuilder()
//                    .receiveIdType("open_id")
//                    .createMessageReqBody(CreateMessageReqBody.newBuilder()
//                            .receiveId(sentTo)
//                            .msgType("interactive")
//                            .content(msgContent)
//                            .uuid(IdUtil.fastUUID())
//                            .build())
//                    .build();
//
//            // 发起请求
//            CreateMessageResp resp = client.im().v1().message().create(req);
//            if (0 != resp.getCode()) {
//                return new MsgSendResult(false, "发送飞书应用消息失败: " + resp.getMsg());
//            } else {
//                return new MsgSendResult(true, "发送飞书应用消息成功");
//            }
//        } catch (Exception e) {
//            log.error("发送飞书应用消息异常", e);
//            return new MsgSendResult(false, "发送飞书应用消息异常: " + e.getMessage());
//        }
        return new MsgSendResult(false, "飞书应用消息发送暂不支持");
    }


}
