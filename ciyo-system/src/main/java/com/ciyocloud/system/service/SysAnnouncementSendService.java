package com.ciyocloud.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.system.entity.SysAnnounceSendEntity;
import com.ciyocloud.system.vo.AnnouncementSendVO;
import com.ciyocloud.system.vo.UnReadAnnouncementVO;

import java.util.List;
import java.util.Map;

/**
 * 通知公告
 *
 * @author codeck
 */
public interface SysAnnouncementSendService extends IService<SysAnnounceSendEntity> {


    /**
     * 查询我的全部消息
     */
    Page<AnnouncementSendVO> getMyAnnouncementSendPage(Page<AnnouncementSendVO> page, AnnouncementSendVO announcementSendModel);

    /**
     * 查询我未读消息列表
     *
     * @return 未读消息列表
     */
    Result<List<UnReadAnnouncementVO>> getUnReadAnnouncementList();

    /**
     * 查询我的消息列表 (消息类型)
     */
    Result<Map<String, Object>> getAnnounceNotice();
}
