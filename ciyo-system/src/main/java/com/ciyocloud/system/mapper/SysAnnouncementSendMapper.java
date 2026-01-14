package com.ciyocloud.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.system.entity.SysAnnounceSendEntity;
import com.ciyocloud.system.vo.AnnouncementSendVO;
import com.ciyocloud.system.vo.UnReadAnnouncementVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知公告
 *
 * @author codeck
 */
public interface SysAnnouncementSendMapper extends BaseMapper<SysAnnounceSendEntity> {


    /**
     * 分页获取我的消息
     */
    List<AnnouncementSendVO> selectMyAnnouncementSendList(@Param("page") Page<AnnouncementSendVO> page, @Param(Constants.WRAPPER) Wrapper<SysAnnounceSendEntity> queryWrapper);


    /**
     * 查询我未读消息列表
     *
     * @return 未读消息列表
     */
    List<UnReadAnnouncementVO> selectUnReadAnnouncementList(@Param("userId") Long userId, @Param("msgCategory") String msgCategory);
}
