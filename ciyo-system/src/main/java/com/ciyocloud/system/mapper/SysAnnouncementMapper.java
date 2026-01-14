package com.ciyocloud.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.system.entity.SysAnnounceEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 通知公告
 *
 * @author codeck
 */
public interface SysAnnouncementMapper extends BaseMapper<SysAnnounceEntity> {


    @Select("  select * from sys_announcement " +
            " where send_status = '2' " +
            "  and del_flag = '0' " +
            "  and msg_category = #{msgCategory} " +
            "  and id IN ( select annt_id from sys_announcement_send where user_id = #{userId} and read_flag = '0') " +
            "  order by create_time DESC")
    List<SysAnnounceEntity> selectSysCementListByUserId(Page<SysAnnounceEntity> page, @Param("userId") Long userId, @Param("msgCategory") String msgCategory);

}
