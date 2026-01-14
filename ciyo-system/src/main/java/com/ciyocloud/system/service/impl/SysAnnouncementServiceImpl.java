package com.ciyocloud.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.event.SendInternalMsgEvent;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.message.websocket.WsMessage;
import com.ciyocloud.message.websocket.WsSessionManager;
import com.ciyocloud.system.entity.SysAnnounceEntity;
import com.ciyocloud.system.entity.SysAnnounceSendEntity;
import com.ciyocloud.system.mapper.SysAnnouncementMapper;
import com.ciyocloud.system.mapper.SysAnnouncementSendMapper;
import com.ciyocloud.system.service.SysAnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 通知公告
 *
 * @author codeck
 */
@Service
public class SysAnnouncementServiceImpl extends ServiceImpl<SysAnnouncementMapper, SysAnnounceEntity> implements SysAnnouncementService {

    @Autowired
    private SysAnnouncementMapper sysAnnouncementMapper;

    @Autowired
    private SysAnnouncementSendMapper sysAnnouncementSendMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAnnouncement(SysAnnounceEntity sysAnnouncement) {
        if (sysAnnouncement.getMsgType().equals(SysAnnounceEntity.MsgTypeEnum.ALL)) {
            sysAnnouncementMapper.insert(sysAnnouncement);
        } else {
            // 1.插入通告表记录
            sysAnnouncementMapper.insert(sysAnnouncement);
            // 2.插入用户通告阅读标记表记录
            String[] userIds = sysAnnouncement.getUserIds().split(",");
            Long anntId = sysAnnouncement.getId();
            LocalDateTime refDate = LocalDateTime.now();
            for (String userId : userIds) {
                SysAnnounceSendEntity announcementSend = new SysAnnounceSendEntity();
                announcementSend.setAnntId(anntId);
                announcementSend.setUserId(Long.valueOf(userId));
                announcementSend.setReadFlag("0");
                announcementSend.setReadTime(refDate);
                sysAnnouncementSendMapper.insert(announcementSend);
            }
        }
    }

    /**
     * 编辑消息信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateAnnouncement(SysAnnounceEntity sysAnnouncement) {
        // 1.更新系统信息表数据
        sysAnnouncementMapper.updateById(sysAnnouncement);
        String userId = sysAnnouncement.getUserIds();
        if (StrUtil.isNotEmpty(userId) && sysAnnouncement.getMsgType().equals(SysAnnounceEntity.MsgTypeEnum.USER)) {
            // 2.补充新的通知用户数据
            String[] userIds = sysAnnouncement.getUserIds().split(",");
            Long anntId = sysAnnouncement.getId();
            LocalDateTime refDate = LocalDateTime.now();
            for (String id : userIds) {
                LambdaQueryWrapper<SysAnnounceSendEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SysAnnounceSendEntity::getAnntId, anntId);
                queryWrapper.eq(SysAnnounceSendEntity::getUserId, id);
                List<SysAnnounceSendEntity> announcementSends = sysAnnouncementSendMapper.selectList(queryWrapper);
                if (announcementSends.size() <= 0) {
                    SysAnnounceSendEntity announcementSend = new SysAnnounceSendEntity();
                    announcementSend.setAnntId(anntId);
                    announcementSend.setUserId(Long.parseLong(id));
                    announcementSend.setReadFlag("0");
                    announcementSend.setReadTime(refDate);
                    sysAnnouncementSendMapper.insert(announcementSend);
                }
            }
            // 3. 删除多余通知用户数据
            Collection<String> delUserIds = Arrays.asList(userIds);
            LambdaQueryWrapper<SysAnnounceSendEntity> queryWrapper = new LambdaQueryWrapper<SysAnnounceSendEntity>();
            queryWrapper.notIn(SysAnnounceSendEntity::getUserId, delUserIds);
            queryWrapper.eq(SysAnnounceSendEntity::getAnntId, anntId);
            sysAnnouncementSendMapper.delete(queryWrapper);
        }
        return true;
    }

    /**
     * 流程执行完成保存消息通知
     *
     * @param title      消息标题
     * @param msgContent 消息内容
     */
    @Override
    public void saveSysAnnouncement(String title, String msgContent) {
        SysAnnounceEntity announcement = new SysAnnounceEntity();
        announcement.setTitle(title);
        announcement.setMsgContent(msgContent);
        announcement.setSender("Tduck");
        announcement.setPriority(SysAnnounceEntity.PriorityEnum.PRIORITY_L);
        announcement.setMsgType(SysAnnounceEntity.MsgTypeEnum.ALL);
        announcement.setSendStatus(SysAnnounceEntity.SendStatusEnum.HAS_SEND);
        announcement.setSendTime(LocalDateTime.now());
        announcement.setDelFlag("0");
        sysAnnouncementMapper.insert(announcement);
    }


    /**
     * 发送站内信消息
     */
    @EventListener
    public void sendSysAnnouncement(SendInternalMsgEvent event) {
        SysAnnounceEntity announcement = new SysAnnounceEntity();
        announcement.setTitle(event.getTitle());
        announcement.setMsgContent(event.getMsgContent());
        announcement.setSender("admin");
        announcement.setPriority(SysAnnounceEntity.PriorityEnum.PRIORITY_M);
        announcement.setMsgType(SysAnnounceEntity.MsgTypeEnum.USER);
        announcement.setSendStatus(SysAnnounceEntity.SendStatusEnum.HAS_SEND);
        announcement.setSendTime(LocalDateTime.now());
        announcement.setDelFlag("0");
        announcement.setUserIds(String.valueOf(event.getUserId()));
        announcement.setBusType("system");
        announcement.setBusId(event.getSourceId());
        announcement.setOpenType("default");
        announcement.setOpenPage(event.getOpenUrl());
        SpringContextUtils.getBean(SysAnnouncementService.class).save(announcement);
        // 插入未读消息记录
        SysAnnounceSendEntity sysAnnounceSend = new SysAnnounceSendEntity();
        sysAnnounceSend.setAnntId(announcement.getId());
        sysAnnounceSend.setUserId(event.getUserId());
        sysAnnounceSend.setReadFlag("0");
        sysAnnouncementSendMapper.insert(sysAnnounceSend);
        WsSessionManager.sendMessage(event.getUserId().toString(), WsMessage.builder().msgType(WsMessage.MsgType.SYS_MSG).body(announcement).build());
    }


}
