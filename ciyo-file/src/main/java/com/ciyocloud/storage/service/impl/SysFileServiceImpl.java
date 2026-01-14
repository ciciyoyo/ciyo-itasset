package com.ciyocloud.storage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.storage.entity.SysFileEntity;
import com.ciyocloud.storage.mapper.SysFileMapper;
import com.ciyocloud.storage.service.SysFileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统文件表服务实现类
 *
 * @author ciyo
 */
@Service
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFileEntity> implements SysFileService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveFile(SysFileEntity sysFile) {
        return this.save(sysFile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBind(java.util.List<Long> fileIds, String bizId, String bizType, String bizField) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        this.lambdaUpdate()
                .in(SysFileEntity::getId, fileIds)
                .set(SysFileEntity::getBizId, bizId)
                .set(SysFileEntity::getBizType, bizType)
                .set(bizField != null, SysFileEntity::getBizField, bizField)
                .set(SysFileEntity::getIsTemp, 0) // 设置为非临时文件
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBindByUrl(java.util.List<String> fileUrls, String bizId, String bizType, String bizField) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }
        this.lambdaUpdate()
                .in(SysFileEntity::getFileUrl, fileUrls)
                .set(SysFileEntity::getBizId, bizId)
                .set(SysFileEntity::getBizType, bizType)
                .set(bizField != null, SysFileEntity::getBizField, bizField)
                .set(SysFileEntity::getIsTemp, 0) // 设置为非临时文件
                .update();
    }
}
