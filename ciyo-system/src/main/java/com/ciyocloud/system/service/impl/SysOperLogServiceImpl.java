package com.ciyocloud.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.oplog.event.OperLogEvent;
import com.ciyocloud.system.entity.SysOperLogEntity;
import com.ciyocloud.system.mapper.SysOperLogMapper;
import com.ciyocloud.system.service.SysOperLogService;
import com.ciyocloud.system.vo.SystemEntityConvert;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志 服务层处理
 *
 * @author codeck
 */
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLogEntity> implements SysOperLogService {


    /**
     * 操作日志记录
     *
     * @param operLogEvent 操作日志事件
     */
    @Async
    @EventListener
    public void recordOper(OperLogEvent operLogEvent) {
        SysOperLogEntity operLog = SystemEntityConvert.INSTANCE.convertSysOprLog(operLogEvent);
        // 远程查询操作地点
//        operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
        operLog.setOperTime(LocalDateTime.now());
        save(operLog);
    }

    @Override
    public void cleanOperLog() {
        baseMapper.cleanOperLog();
    }
}
