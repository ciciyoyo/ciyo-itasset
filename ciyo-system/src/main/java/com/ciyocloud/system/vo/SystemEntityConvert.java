package com.ciyocloud.system.vo;

import com.ciyocloud.oplog.event.OperLogEvent;
import com.ciyocloud.system.entity.SysAnnounceEntity;
import com.ciyocloud.system.entity.SysOperLogEntity;
import com.ciyocloud.system.entity.SysUserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * VO类转换器
 *
 * @author : codeck
 * @since :  2023/03/24 11:13
 **/
@Mapper
public interface SystemEntityConvert {

    SystemEntityConvert INSTANCE = Mappers.getMapper(SystemEntityConvert.class);


    /**
     * 操作日志事件转换
     *
     * @param operLogEvent 操作日志事件
     * @return 操作日志
     */
    SysOperLogEntity convertSysOprLog(OperLogEvent operLogEvent);


    /**
     * 用戶轉換
     */
    SysUserVO convertSysUser(SysUserEntity sysUserEntity);


    /**
     * 消息通知
     */
    SysAnnounceVO convertSysAnnounce(SysAnnounceEntity sysAnnounceEntity);

}
