package com.ciyocloud.message.util;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.ciyocloud.common.util.JsonUtils;
import com.ciyocloud.common.util.ShortIdUtils;
import com.ciyocloud.message.entity.MsgSendResult;
import com.ciyocloud.message.entity.SysMessageEntity;
import com.ciyocloud.message.entity.SysMsgSendDetailEntity;
import com.ciyocloud.message.entity.SysMsgTemplateEntity;
import com.ciyocloud.message.entity.enums.MsgTypeEnum;
import com.ciyocloud.message.handle.SendMsgHandle;
import com.ciyocloud.message.handle.impl.EmailSendMsgHandle;
import com.ciyocloud.message.handle.impl.InternalSendMsgHandle;
import com.ciyocloud.message.handle.impl.SmsSendMsgHandle;
import com.ciyocloud.message.handle.impl.WxSendMsgHandle;
import com.ciyocloud.message.request.BatchSendRequest;
import com.ciyocloud.message.service.SysMessageService;
import com.ciyocloud.message.service.SysMsgSendDetailService;
import com.ciyocloud.message.service.SysMsgTemplateService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息生成工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PushMsgUtils {

    public static String T_MSG_ID = "tMsgId";
    public static String T_USER_NAME = "tUserName";
    private final SysMessageService sysMessageService;
    private final SysMsgTemplateService sysMessageTemplateService;
    private final SysMsgSendDetailService sysMsgSendDetailService;
    private final SmsSendMsgHandle smsSendMsgHandle;
    private final ConcurrentHashMap<MsgTypeEnum, SendMsgHandle> sendHandles = new ConcurrentHashMap<>();

    /**
     * 模板字符串变量替换
     *
     * @param templateString 模板字符串 比如：Hello, ${name}!
     * @param variables      变量 map
     * @return 替换后的字符串 比如：Hello, John Doe!
     */
    private static String replaceVariables(String templateString, Map<String, Object> variables) {
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
        Matcher matcher = pattern.matcher(templateString);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String variable = matcher.group(1);
            Object replacement = variables.get(variable);
            matcher.appendReplacement(buffer, null != replacement ? Convert.toStr(replacement) : StrUtil.EMPTY);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    @PostConstruct
    public void init() {
        sendHandles.put(MsgTypeEnum.EMAIL, new EmailSendMsgHandle());
        sendHandles.put(MsgTypeEnum.SMS, smsSendMsgHandle);
        sendHandles.put(MsgTypeEnum.WX_MP, new WxSendMsgHandle());
        sendHandles.put(MsgTypeEnum.INTERNAL, new InternalSendMsgHandle());
//        sendHandles.put(MsgTypeEnum.WX_CP, new WxCpSendMsgHandle());
//        sendHandles.put(MsgTypeEnum.CHAT_HOOK, new ChatHookSendMsgHandle());
//        sendHandles.put(MsgTypeEnum.DING_TALK, new DingTalkSendMsgHandle());
//        sendHandles.put(MsgTypeEnum.LARK, new LarkSendMsgHandle());
    }

    /**
     * @param msgType      消息类型 1短信 2邮件 3微信 4站内信
     * @param templateCode 消息模板码
     * @param map          消息参数
     * @param sentTo       接收消息方
     */
    @Async
    public void sendMessageByTemplateCode(MsgTypeEnum msgType, String templateCode, Map<String, Object> map,
                                          String sentTo) {
        SysMsgTemplateEntity sysSmsTemplate = sysMessageTemplateService.getByCode(templateCode);
        sysSmsTemplate.setTemplateType(msgType);
        this.sendSingleMessageAndSave(sysSmsTemplate, map, sentTo);
    }

    /**
     * 发送消息 根据模版id和参数
     *
     * @param templateId 模版id
     * @param map        参数
     * @param sentTo     发送人
     * @return 是否成功
     */
    public boolean sendMessageByTemplateId(Long templateId, Map<String, Object> map, String sentTo) {
        SysMsgTemplateEntity sysSmsTemplate = sysMessageTemplateService.getById(templateId);
        return this.sendSingleMessageAndSave(sysSmsTemplate, map, sentTo);
    }

    /**
     * 发送消息 包含渲染模板的功能呢
     *
     * @param sysMsgTemplate 模版Id
     * @param map            动态参数
     * @param sentTo         发送人
     * @return 是否成功
     */
    public boolean sendSingleMessageAndSave(SysMsgTemplateEntity sysMsgTemplate, Map<String, Object> map,
                                            String sentTo) {
        String msgId = ShortIdUtils.genId();
        // 这里发短信先忽略掉 不然腾讯云多传就报错了
        if (MapUtil.isNotEmpty(map) && sysMsgTemplate.getTemplateType() != MsgTypeEnum.SMS) {
            map.put("msgId", msgId);
        }
        sysMsgTemplate.setTemplateContent(renderTemplateContent(sysMsgTemplate, map));
        SysMessageEntity sysMessageEntity = sendMessageByTemplate(sysMsgTemplate, map, msgId, sentTo);
        sysMessageService.save(sysMessageEntity);
        return sysMessageEntity.getSendStatus() == SysMessageEntity.SendStatus.SUCCESS;
    }

    /**
     * 根据模板发送消息 调用消息渠道
     *
     * @param sysMsgTemplate 消息模板
     * @param map            消息参数
     * @param msgId          消息id
     * @param sentTo         接收人
     * @return 消息对象
     */
    public SysMessageEntity sendMessageByTemplate(SysMsgTemplateEntity sysMsgTemplate, Map<String, Object> map,
                                                  String msgId, String sentTo) {
        SysMessageEntity sysMessage = new SysMessageEntity();
        if (ObjectUtil.isNotNull(sysMsgTemplate)) {
            return sendMessage(sysMsgTemplate.getTemplateType(), sysMsgTemplate.getTemplateName(),
                    sysMsgTemplate.getTemplateContent(), map, msgId, sentTo, sysMsgTemplate.getThirdTemplateId());
        }
        return sysMessage;
    }

    /**
     * 发送消息 调用消息渠道
     *
     * @param msgType 消息类型
     * @param title   消息标题
     * @param content 消息内容
     * @param params  消息参数
     * @param msgId   消息id
     * @param sentTo  接收人
     * @return 消息对象
     */
    public SysMessageEntity sendMessage(MsgTypeEnum msgType, String title, String content, Map<String, Object> params,
                                        String msgId, String sentTo, String ThirdTemplateId) {
        SysMessageEntity sysMessage = new SysMessageEntity();
        sysMessage.setType(msgType);
        sysMessage.setReceiver(sentTo);
        sysMessage.setTitle(title);
        sysMessage.setContent(content);
        sysMessage.setMsgParams(JsonUtils.objToJson(params));
        sysMessage.setSendTime(LocalDateTime.now());
        sysMessage.setMsgId(StrUtil.blankToDefault(msgId, IdUtil.getSnowflakeNextIdStr()));
        try {
            MsgSendResult msgSendResult = sendHandles.get(msgType).sendMsg(sentTo, title, sysMessage.getContent(),
                    ThirdTemplateId, params);
            if (msgSendResult.getSuccess()) {
                sysMessage.setSendStatus(SysMessageEntity.SendStatus.SUCCESS);
                sysMessage.setResult(msgSendResult.getResult());
            } else {
                sysMessage.setSendStatus(SysMessageEntity.SendStatus.FAIL);
                sysMessage.setResult(msgSendResult.getResult());
            }
        } catch (Exception e) {

            sysMessage.setSendStatus(SysMessageEntity.SendStatus.FAIL);
            sysMessage.setResult(e.getMessage());
        }
        sysMessage.setSendNum(0);
        return sysMessage;
    }

    /**
     * 渲染模板内容
     */
    public String renderTemplateContent(SysMsgTemplateEntity sysMsgTemplate, Map<String, Object> map) {
        // 模板内容 不支持的需要移除掉Html标签
        String content = sysMsgTemplate.getTemplateContent();
        if (!ListUtil.of(MsgTypeEnum.INTERNAL, MsgTypeEnum.EMAIL).contains(sysMsgTemplate.getTemplateType())) {
            content = HtmlUtil.cleanHtmlTag(content);
        }
        return replaceVariables(content, map);
    }

    /**
     * 批量发送消息(多线程版本)
     *
     * @param request 批量发送请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchSendMessageAndSaveDetail(BatchSendRequest request) {
        // 获取模板
        SysMsgTemplateEntity templateEntity = sysMessageTemplateService.getById(request.getTemplateId());
        String originalTemplateContent = templateEntity.getTemplateContent();

        // 线程安全的消息列表
        List<SysMessageEntity> allMessages = new CopyOnWriteArrayList<>();

        // 并行处理每个接收人
        request.getReceiverList().parallelStream().forEach(receiver -> {
            try {
                String msgId = ShortIdUtils.genId();

                // 每个接收者独立的参数和模板副本
                Map<String, Object> msgParams = new HashMap<>(request.getMsgParams() == null ? new HashMap<>() : request.getMsgParams());
                SysMsgTemplateEntity templateCopy = new SysMsgTemplateEntity();
                BeanUtils.copyProperties(templateEntity, templateCopy);
                templateCopy.setTemplateContent(originalTemplateContent);

                // 特殊变量处理
                if (MapUtil.isNotEmpty(msgParams)) {
                    msgParams.put(T_MSG_ID, msgId);
                    msgParams.put(T_USER_NAME, receiver.getUserName());
                    if (request.getIsVariableValue()) {
                        // 先收集需要替换的key-value，避免遍历时修改Map导致并发问题
                        Map<String, Object> replacedValues = new HashMap<>();
                        msgParams.forEach((key, value) -> {
                            if (value instanceof String && ((String) value).contains("$")) {
                                replacedValues.put(key, replaceVariables((String) value, msgParams));
                            }
                        });
                        msgParams.putAll(replacedValues);
                    }
                }

                templateCopy.setTemplateContent(renderTemplateContent(templateCopy, msgParams));
                SysMessageEntity sysMessageEntity = sendMessageByTemplate(templateCopy, msgParams, msgId, receiver.getId());
                sysMessageEntity.setExtStatus(0);
                allMessages.add(sysMessageEntity);
            } catch (Exception e) {
                log.error("批量发送消息失败", e);
                SysMessageEntity sysMessageEntity = new SysMessageEntity();
                sysMessageEntity.setSendStatus(SysMessageEntity.SendStatus.FAIL);
                sysMessageEntity.setResult(e.getMessage());
                sysMessageEntity.setReceiver(receiver.getId());
                sysMessageEntity.setSendTime(LocalDateTime.now());
                sysMessageEntity.setType(templateEntity.getTemplateType());
                sysMessageEntity.setMsgId(ShortIdUtils.genId());
                sysMessageEntity.setTitle(templateEntity.getTemplateName());
                sysMessageEntity.setContent(templateEntity.getTemplateContent());
                sysMessageEntity.setSendNum(0);
                sysMessageEntity.setExtStatus(0);
                allMessages.add(sysMessageEntity);
            }
        });

        // 保存发送详情记录
        SysMsgSendDetailEntity sysMsgSendDetailEntity = new SysMsgSendDetailEntity();
        sysMsgSendDetailEntity.setTemplateId(request.getTemplateId());
        sysMsgSendDetailEntity.setSourceType(request.getSourceType());
        sysMsgSendDetailEntity.setSourceId(request.getSourceId());
        sysMsgSendDetailEntity.setSendCount((long) allMessages.size());
        sysMsgSendDetailEntity.setMsgType(templateEntity.getTemplateType());
        sysMsgSendDetailEntity.setMsgContent(originalTemplateContent);
        sysMsgSendDetailService.save(sysMsgSendDetailEntity);

        // 给所有消息设置发送详情ID
        allMessages.forEach(msg -> msg.setSendDetailId(sysMsgSendDetailEntity.getId()));

        // 批量保存,每200条一批
        int batchSize = 200;
        for (int i = 0; i < allMessages.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allMessages.size());
            sysMessageService.saveBatch(allMessages.subList(i, end));
        }
    }

}
