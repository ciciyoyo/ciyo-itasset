package com.ciyocloud.itam.service.impl;

import com.ciyocloud.common.mybatis.service.BaseServiceImpl;
import com.ciyocloud.common.sse.util.SseAsyncProcessUtils;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.SuppliersEntity;
import com.ciyocloud.itam.listener.SuppliersImportListener;
import com.ciyocloud.itam.mapper.SuppliersMapper;
import com.ciyocloud.itam.service.SuppliersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * 供应商Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
@Slf4j
@Service
public class SuppliersServiceImpl extends BaseServiceImpl<SuppliersMapper, SuppliersEntity> implements SuppliersService {

    @Override
    public void importData(InputStream inputStream, String originalFilename, String progressKey, Long userId) {
        try {
            log.info("开始导入供应商数据，progressKey: {}, userId: {}, 文件名: {}", progressKey, userId, originalFilename);

            // 初始化进度
            SseAsyncProcessUtils.setTips("正在解析Excel文件...");

            // 创建支持SSE进度推送的导入监听器
            SuppliersImportListener listener = new SuppliersImportListener(progressKey, userId);
            // 更新进度：开始导入
            SseAsyncProcessUtils.setProcess(0, "开始导入供应商数据...");
            //执行导入
            ExcelUtils.importExcel(inputStream, SuppliersEntity.class, listener);
        } catch (Exception e) {
            log.error("供应商数据导入失败，progressKey: {}, 错误: {}", progressKey, e.getMessage(), e);
            SseAsyncProcessUtils.setError("导入失败: " + e.getMessage());
            throw e;
        }
    }

}
