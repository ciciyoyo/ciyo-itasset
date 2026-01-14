package com.ciyocloud.message.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.message.entity.SysMessageEntity;
import com.ciyocloud.message.event.UpdateMsgExtStatusEvent;
import com.ciyocloud.message.mapper.SysMessageMapper;
import com.ciyocloud.message.service.SysMessageService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author : codeck
 * @description : 消息
 * @create :  2021/06/07 16:37
 **/
@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessageEntity> implements SysMessageService {


    @EventListener
    @Async
    public void onUpdateMsgExtStatusEvent(UpdateMsgExtStatusEvent event) {
        // 修改状态
        this.update(Wrappers.<SysMessageEntity>lambdaUpdate()
                .set(SysMessageEntity::getExtStatus, event.getStatus())
                .eq(SysMessageEntity::getMsgId, event.getMsgId()));
    }

}
