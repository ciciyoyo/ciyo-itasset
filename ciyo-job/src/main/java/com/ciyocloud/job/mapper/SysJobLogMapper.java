package com.ciyocloud.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ciyocloud.job.entity.SysJobLogEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 调度任务日志信息 数据层
 *
 * @author codeck
 */
public interface SysJobLogMapper extends BaseMapper<SysJobLogEntity> {


    @Delete("delete from sys_job_log where create_time < #{createTime}")
    void cleanJobLog(@Param("createTime") LocalDateTime createTime);
}
