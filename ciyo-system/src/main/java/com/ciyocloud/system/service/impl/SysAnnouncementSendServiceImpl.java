package com.ciyocloud.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.system.entity.SysAnnounceEntity;
import com.ciyocloud.system.entity.SysAnnounceSendEntity;
import com.ciyocloud.system.mapper.SysAnnouncementSendMapper;
import com.ciyocloud.system.service.SysAnnouncementSendService;
import com.ciyocloud.system.service.SysAnnouncementService;
import com.ciyocloud.system.vo.AnnouncementSendVO;
import com.ciyocloud.system.vo.UnReadAnnouncementVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知公告
 *
 * @author codeck
 */
@Service
public class SysAnnouncementSendServiceImpl extends ServiceImpl<SysAnnouncementSendMapper, SysAnnounceSendEntity> implements SysAnnouncementSendService {

    @Autowired
    private SysAnnouncementSendMapper sysAnnouncementSendMapper;
    @Autowired
    private SysAnnouncementService sysAnnouncementService;


    @Override
    public Page<AnnouncementSendVO> getMyAnnouncementSendPage(Page<AnnouncementSendVO> page, AnnouncementSendVO send) {
        QueryWrapper<SysAnnounceSendEntity> wrapper = Wrappers.query();
        boolean isNoBpm = StrUtil.isNotBlank(send.getBizSource()) && "isNoBpm".equals(send.getBizSource());
        wrapper.eq("sa.send_status", "2")
                .eq("sa.del_flag", "0")
                .eq("sas.user_id", send.getUserId())
                .eq(StrUtil.isNotBlank(send.getTitle()), "sa.title", send.getTitle())
                .eq(StrUtil.isNotBlank(send.getSender()), "sa.sender", send.getSender())
                .eq(null != send.getReadFlag(), "sas.read_flag", send.getReadFlag())
                .eq(StrUtil.isNotBlank(send.getBusType()), "sa.bus_type", send.getBusType())
                .eq(null != send.getMsgCategory(), "sa.msg_category", send.getMsgCategory())
                .and(isNoBpm, x -> {
                    x.ne("sa.bus_type", "bpm").or().isNull("sa.bus_type");
                }).orderByAsc("sas.read_flag").orderByDesc("sa.send_time");
        return page.setRecords(sysAnnouncementSendMapper.selectMyAnnouncementSendList(page, wrapper));
    }

    @Override
    public Result<List<UnReadAnnouncementVO>> getUnReadAnnouncementList() {
        Long userId = SecurityUtils.getUserId();
        List<UnReadAnnouncementVO> announcementList = baseMapper.selectUnReadAnnouncementList(userId, null);
        return Result.success(announcementList);
    }

    @Override
    public Result<Map<String, Object>> getAnnounceNotice() {
        Long userId = SecurityUtils.getUserId();
        // 将接受人为全部用户的消息查询出来 插入到消息读表中 避免发送时一次插入多条数据
        LambdaQueryWrapper<SysAnnounceEntity> querySaWrapper = new LambdaQueryWrapper<>();
        querySaWrapper.eq(SysAnnounceEntity::getMsgType, SysAnnounceEntity.MsgTypeEnum.ALL.getValue());
        querySaWrapper.eq(SysAnnounceEntity::getDelFlag, "0");
        querySaWrapper.eq(SysAnnounceEntity::getSendStatus, SysAnnounceEntity.SendStatusEnum.HAS_SEND.getValue());
        //新注册用户不看结束通知
        querySaWrapper.ge(SysAnnounceEntity::getEndTime, SecurityUtils.getLoginUser().getUser().getCreateTime());
        // 避免插入重复数据
        querySaWrapper.notInSql(SysAnnounceEntity::getId, "select annt_id from sys_announcement_send where user_id=" + userId);
        List<SysAnnounceEntity> announcements = sysAnnouncementService.list(querySaWrapper);
        if (!announcements.isEmpty()) {
            // 插入到消息读表中
            List<SysAnnounceSendEntity> announcementSendList = announcements.stream().map(announcement -> {
                LambdaQueryWrapper<SysAnnounceSendEntity> query = new LambdaQueryWrapper<>();
                query.eq(SysAnnounceSendEntity::getAnntId, announcement.getId());
                query.eq(SysAnnounceSendEntity::getUserId, userId);
                SysAnnounceSendEntity one = this.getOne(query);
                if (null == one) {
                    SysAnnounceSendEntity announcementSend = new SysAnnounceSendEntity();
                    announcementSend.setAnntId(announcement.getId());
                    announcementSend.setUserId(userId);
                    announcementSend.setReadFlag("0");
                    return announcementSend;
                }
                return null;
            }).collect(Collectors.toList());
            this.saveBatch(announcementSendList);
        }
        // 2.查询用户未读的系统消息
        //通知公告消息
        List<UnReadAnnouncementVO> anntMsgList = baseMapper.selectUnReadAnnouncementList(userId, SysAnnounceEntity.MsgCategoryEnum.NOTICE.getValue());
        //系统消息
        List<UnReadAnnouncementVO> sysMsgList = baseMapper.selectUnReadAnnouncementList(userId, SysAnnounceEntity.MsgCategoryEnum.SYSTEM.getValue());
        Map<String, Object> sysMsgMap = new HashMap<>();
        sysMsgMap.put("sysMsgList", sysMsgList);
        sysMsgMap.put("sysMsgTotal", sysMsgList.size());
        sysMsgMap.put("anntMsgList", anntMsgList);
        sysMsgMap.put("anntMsgTotal", anntMsgList.size());
        return Result.success(sysMsgMap);
    }


}
