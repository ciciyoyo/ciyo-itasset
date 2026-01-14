package com.ciyocloud.message.request;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 消息批量发送请求
 *
 * @author : codeck
 * @since :  2023/12/23 21:42
 **/
@Data
public class BatchSendRequest {

    // 通知分享
    public static String NOTIFY_SHARE = "NOTIFY_SHARE";
    /**
     * 模板ID
     */
    private Long templateId;
    /**
     * 消息类型
     */
    private List<ReceiverUser> receiverList;
    /**
     * 消息类型
     */
    private Map<String, Object> msgParams;
    /**
     * 来源类型
     */
    private String sourceType;
    /**
     * 来源Id
     */
    private String sourceId;
    /**
     * 值是否是变量 适用于批量发送时  比如用户名 唯一链接等情况
     */
    private Boolean isVariableValue = false;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiverUser {
        private String id;

        private String userName;

        public String getUserName() {
            return StrUtil.blankToDefault(userName, "用户");
        }
    }


}
