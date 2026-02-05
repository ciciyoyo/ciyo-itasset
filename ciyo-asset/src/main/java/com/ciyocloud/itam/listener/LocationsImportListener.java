package com.ciyocloud.itam.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.excel.core.SseProgressExcelListener;
import com.ciyocloud.itam.entity.LocationsEntity;
import com.ciyocloud.itam.service.LocationsService;
import lombok.extern.slf4j.Slf4j;

/**
 * 物理位置SSE进度导入监听器
 *
 * @author codeck
 * @since 2026-01-30
 */
@Slf4j
public class LocationsImportListener extends SseProgressExcelListener<LocationsEntity> {

    private final LocationsService locationsService;
    private final Long operatorId;

    public LocationsImportListener(String progressKey, Long userId) {
        super(progressKey, userId, true, 50, 1000);
        this.locationsService = SpringContextUtils.getBean(LocationsService.class);
        this.operatorId = userId;
    }

    @Override
    public void processData(LocationsEntity location, AnalysisContext context) {
        int currentRow = context.readRowHolder().getRowIndex() + 1;

        // 验证必填字段
        if (StrUtil.isBlank(location.getName())) {
            throw new BusinessException(String.format("第%d行 位置名称不能为空", currentRow));
        }

        // 根据父级位置名称查找父级ID
        if (StrUtil.isNotBlank(location.getParentName())) {
            LocationsEntity parent = locationsService.getOneSafe(
                    new LambdaQueryWrapper<LocationsEntity>()
                            .eq(LocationsEntity::getName, location.getParentName())
            );
            if (parent == null) {
                throw new BusinessException(String.format("第%d行 父级位置 '%s' 不存在", currentRow, location.getParentName()));
            }
            location.setParentId(parent.getId());
        } else {
            // 没有父级名称，设为顶级节点
            location.setParentId(null);
        }

        // 检查同一父级下位置名称是否已存在
        LambdaQueryWrapper<LocationsEntity> wrapper = new LambdaQueryWrapper<LocationsEntity>()
                .eq(LocationsEntity::getName, location.getName());
        
        // 如果有父级，需要在同一父级下检查唯一性
        if (location.getParentId() != null) {
            wrapper.eq(LocationsEntity::getParentId, location.getParentId());
        } else {
            wrapper.isNull(LocationsEntity::getParentId);
        }
        
        LocationsEntity existing = locationsService.getOneSafe(wrapper);

        if (ObjectUtil.isNull(existing)) {
            // 新增位置
            location.setCreateBy(operatorId);
            location.setDeleted(0);
            locationsService.save(location);
        } else {
            // 根据导入数据是否有ID来决定是更新还是跳过
            if (location.getId() != null && location.getId().equals(existing.getId())) {
                // 更新位置
                location.setUpdateBy(operatorId);
                // 保留创建信息
                location.setCreateBy(existing.getCreateBy());
                location.setCreateTime(existing.getCreateTime());
                location.setDeleted(existing.getDeleted());
                locationsService.updateById(location);
            } else {
                String parentInfo = location.getParentId() != null ? 
                    String.format("父级位置 '%s' 下", location.getParentName()) : "顶级";
                throw new BusinessException(String.format("%s位置 '%s' 已存在", parentInfo, existing.getName()));
            }
        }
    }
}