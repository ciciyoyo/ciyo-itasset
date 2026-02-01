package com.ciyocloud.itam.service.impl;

import com.ciyocloud.common.mybatis.service.BaseServiceImpl;
import com.ciyocloud.common.sse.util.SseAsyncProcessUtils;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.LocationsEntity;
import com.ciyocloud.itam.listener.LocationsImportListener;
import com.ciyocloud.itam.mapper.LocationsMapper;
import com.ciyocloud.itam.service.LocationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 物理位置Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
@Slf4j
@Service
public class LocationsServiceImpl extends BaseServiceImpl<LocationsMapper, LocationsEntity> implements LocationsService {


    @Override
    public List<LocationsEntity> getTree(LocationsEntity locations) {
        List<LocationsEntity> entities = this.list(QueryWrapperUtils.toSimpleQuery(locations));
        List<Long> entityIds = entities.stream().map(LocationsEntity::getId).collect(Collectors.toList());
        return entities.stream()
                .filter(e -> e.getParentId() == null || e.getParentId() == 0 || !entityIds.contains(e.getParentId()))
                .peek(e -> e.setChildren(getChildren(e, entities)))
                .collect(Collectors.toList());
    }

    /**
     * 递归获取子节点
     *
     * @param root 父节点
     * @param all  所有节点
     * @return 子节点列表
     */
    private List<LocationsEntity> getChildren(LocationsEntity root, List<LocationsEntity> all) {
        return all.stream()
                .filter(e -> Objects.equals(e.getParentId(), root.getId()))
                .peek(e -> e.setChildren(getChildren(e, all)))
                .collect(Collectors.toList());
    }

    @Override
    public void importData(InputStream inputStream, String originalFilename, String progressKey, Long userId) {
        try {
            log.info("开始导入物理位置数据，progressKey: {}, userId: {}, 文件名: {}", progressKey, userId, originalFilename);

            // 初始化进度
            SseAsyncProcessUtils.setTips("正在解析Excel文件...");

            // 创建支持SSE进度推送的导入监听器
            LocationsImportListener listener = new LocationsImportListener(progressKey, userId);
            // 更新进度：开始导入
            SseAsyncProcessUtils.setTips("开始导入物理位置数据...");
            //执行导入
            ExcelUtils.importExcel(inputStream, LocationsEntity.class, listener);
        } catch (Exception e) {
            log.error("物理位置数据导入失败，progressKey: {}, 错误: {}", progressKey, e.getMessage(), e);
            SseAsyncProcessUtils.setError("导入失败: " + e.getMessage());
            throw e;
        }
    }
}
