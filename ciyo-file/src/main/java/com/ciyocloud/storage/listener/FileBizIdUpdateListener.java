package com.ciyocloud.storage.listener;

import com.ciyocloud.storage.event.FileBizIdUpdateEvent;
import com.ciyocloud.storage.service.SysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 文件业务ID更新事件监听
 *
 * @author ciyo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileBizIdUpdateListener {

    private final SysFileService sysFileService;

    @Async
    @EventListener
    public void onFileBizIdUpdate(FileBizIdUpdateEvent event) {
        log.info("Received FileBizIdUpdateEvent: fileIds={}, fileUrls={}, bizId={}, bizType={}", event.getFileIds(), event.getFileUrls(), event.getBizId(), event.getBizType());
        try {
            if (event.getFileUrls() != null && !event.getFileUrls().isEmpty()) {
                sysFileService.updateBindByUrl(event.getFileUrls(), event.getBizId(), event.getBizType(), event.getBizField());
            } else {
                sysFileService.updateBind(event.getFileIds(), event.getBizId(), event.getBizType(), event.getBizField());
            }
        } catch (Exception e) {
            log.error("Failed to update file biz info", e);
        }
    }
}
