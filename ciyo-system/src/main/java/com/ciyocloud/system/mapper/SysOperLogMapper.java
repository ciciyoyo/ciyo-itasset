package com.ciyocloud.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ciyocloud.system.entity.SysOperLogEntity;
import org.apache.ibatis.annotations.Update;

/**
 * 操作日志 数据层
 *
 * @author codeck
 */
public interface SysOperLogMapper extends BaseMapper<SysOperLogEntity> {

    /**
     * 清空操作日志
     */
    @Update(" truncate table sys_oper_log")
    void cleanOperLog();
}
