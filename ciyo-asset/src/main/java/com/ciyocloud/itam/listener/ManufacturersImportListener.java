package com.ciyocloud.itam.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.excel.core.SseProgressExcelListener;
import com.ciyocloud.itam.entity.ManufacturersEntity;
import com.ciyocloud.itam.service.ManufacturersService;
import lombok.extern.slf4j.Slf4j;

/**
 * 制造商SSE进度导入监听器
 *
 * @author codeck
 * @since 2026-01-28
 */
@Slf4j
public class ManufacturersImportListener extends SseProgressExcelListener<ManufacturersEntity> {

    private final ManufacturersService manufacturersService;
    private final Long operatorId;

    public ManufacturersImportListener(String progressKey, Long userId) {
        super(progressKey, userId, true, 50, 1000);
        this.manufacturersService = SpringContextUtils.getBean(ManufacturersService.class);
        this.operatorId = userId;
    }

    @Override
    public void processData(ManufacturersEntity manufacturer, AnalysisContext context) {
        int currentRow = context.readRowHolder().getRowIndex() + 1;

        // 验证必填字段
        if (StrUtil.isBlank(manufacturer.getName())) {
            throw new BusinessException(String.format("第%d行 制造商名称不能为空", currentRow));
        }

        // 检查制造商名称是否已存在
        ManufacturersEntity existing = manufacturersService.getOneSafe(
                new LambdaQueryWrapper<ManufacturersEntity>()
                        .eq(ManufacturersEntity::getName, manufacturer.getName())
        );

        if (ObjectUtil.isNull(existing)) {
            // 新增制造商
            manufacturer.setCreateBy(operatorId);
            manufacturer.setDeleted(0);
            manufacturersService.save(manufacturer);
        } else {
            // 根据导入数据是否有ID来决定是更新还是跳过
            if (manufacturer.getId() != null && manufacturer.getId().equals(existing.getId())) {
                // 更新制造商
                manufacturer.setUpdateBy(operatorId);
                // 保留创建信息
                manufacturer.setCreateBy(existing.getCreateBy());
                manufacturer.setCreateTime(existing.getCreateTime());
                manufacturer.setDeleted(existing.getDeleted());
                manufacturersService.updateById(manufacturer);
            } else {
                throw new BusinessException(String.format("制造商 %s 已存在", existing.getName()));
            }
        }

    }


}
