package com.ciyocloud.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.system.entity.SysAnnounceEntity;

/**
 * 通知公告
 *
 * @author codeck
 */
public interface SysAnnouncementService extends IService<SysAnnounceEntity> {

    /**
     * 保存消息通知
     *
     * @param sysAnnouncement 公告信息
     */
    void saveAnnouncement(SysAnnounceEntity sysAnnouncement);

    /**
     * 修改消息通知
     *
     * @param sysAnnouncement 公告信息
     */
    boolean updateAnnouncement(SysAnnounceEntity sysAnnouncement);

    /**
     * 保存消息通知
     */
    void saveSysAnnouncement(String title, String msgContent);


}
