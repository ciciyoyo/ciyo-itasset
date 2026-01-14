package com.ciyocloud.storage.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 文件业务ID关联更新事件
 *
 * @author ciyo
 */
@Getter
public class FileBizIdUpdateEvent extends ApplicationEvent {

    /**
     * 文件ID列表
     */
    private final List<Long> fileIds;

    /**
     * 业务ID
     */
    private final String bizId;

    /**
     * 业务类型
     */
    private final String bizType;

    /**
     * 业务字段 (可选)
     */
    private final String bizField;

    /**
     * 文件URL列表
     */
    private final List<String> fileUrls;

    public FileBizIdUpdateEvent(Object source, List<Long> fileIds, String bizId, String bizType, String bizField) {
        super(source);
        this.fileIds = fileIds;
        this.fileUrls = null;
        this.bizId = bizId;
        this.bizType = bizType;
        this.bizField = bizField;
    }

    public FileBizIdUpdateEvent(Object source, List<Long> fileIds, String bizId, String bizType) {
        this(source, fileIds, bizId, bizType, null);
    }

    public FileBizIdUpdateEvent(Object source, List<String> fileUrls, String bizId, String bizType, String bizField, boolean isUrl) {
        super(source);
        this.fileIds = null;
        this.fileUrls = fileUrls;
        this.bizId = bizId;
        this.bizType = bizType;
        this.bizField = bizField;
    }
}
