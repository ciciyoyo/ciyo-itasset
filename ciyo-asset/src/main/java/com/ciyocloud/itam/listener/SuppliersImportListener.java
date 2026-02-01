package com.ciyocloud.itam.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.excel.core.SseProgressExcelListener;
import com.ciyocloud.itam.entity.SuppliersEntity;
import com.ciyocloud.itam.service.SuppliersService;
import lombok.extern.slf4j.Slf4j;

/**
 * 供应商SSE进度导入监听器
 *
 * @author codeck
 * @since 2026-01-30
 */
@Slf4j
public class SuppliersImportListener extends SseProgressExcelListener<SuppliersEntity> {

    private final SuppliersService suppliersService;
    private final Long operatorId;

    public SuppliersImportListener(String progressKey, Long userId) {
        super(progressKey, userId, true, 50, 1000);
        this.suppliersService = SpringContextUtils.getBean(SuppliersService.class);
        this.operatorId = userId;
    }

    @Override
    public void processData(SuppliersEntity supplier, AnalysisContext context) {
        int currentRow = context.readRowHolder().getRowIndex() + 1;

        // 验证必填字段
        if (StrUtil.isBlank(supplier.getName())) {
            throw new BusinessException(String.format("第%d行 供应商名称不能为空", currentRow));
        }

        // 检查供应商名称是否已存在
        SuppliersEntity existing = suppliersService.getOneSafe(
                new LambdaQueryWrapper<SuppliersEntity>()
                        .eq(SuppliersEntity::getName, supplier.getName())
        );

        if (ObjectUtil.isNull(existing)) {
            // 新增供应商
            supplier.setCreateBy(operatorId);
            supplier.setDeleted(0);
            suppliersService.save(supplier);
        } else {
            // 根据导入数据是否有ID来决定是更新还是跳过
            if (supplier.getId() != null && supplier.getId().equals(existing.getId())) {
                // 更新供应商
                supplier.setUpdateBy(operatorId);
                // 保留创建信息
                supplier.setCreateBy(existing.getCreateBy());
                supplier.setCreateTime(existing.getCreateTime());
                supplier.setDeleted(existing.getDeleted());
                suppliersService.updateById(supplier);
            } else {
                throw new BusinessException(String.format("供应商 %s 已存在", existing.getName()));
            }
        }
    }
}