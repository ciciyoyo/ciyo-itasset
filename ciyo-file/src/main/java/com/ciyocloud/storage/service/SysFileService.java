package com.ciyocloud.storage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.storage.entity.SysFileEntity;

/**
 * 系统文件表服务接口
 *
 * @author ciyo
 */
public interface SysFileService extends IService<SysFileEntity> {

    /**
     * 保存文件信息
     *
     * @param sysFile 文件信息
     * @return boolean
     */
    boolean saveFile(SysFileEntity sysFile);

    /**
     * 更新文件业务关联
     *
     * @param fileIds  文件ID列表
     * @param bizId    业务ID
     * @param bizType  业务类型
     * @param bizField 业务字段
     */
    void updateBind(java.util.List<Long> fileIds, String bizId, String bizType, String bizField);

    /**
     * 更新文件业务关联 (通过URL)
     *
     * @param fileUrls 文件URL列表
     * @param bizId    业务ID
     * @param bizType  业务类型
     * @param bizField 业务字段
     */
    void updateBindByUrl(java.util.List<String> fileUrls, String bizId, String bizType, String bizField);


}
