package com.ciyocloud.itam.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.excel.core.SseProgressExcelListener;
import com.ciyocloud.itam.entity.DepreciationsEntity;
import com.ciyocloud.itam.entity.ManufacturersEntity;
import com.ciyocloud.itam.entity.ModelsEntity;
import com.ciyocloud.itam.service.CategoriesService;
import com.ciyocloud.itam.service.DepreciationsService;
import com.ciyocloud.itam.service.ManufacturersService;
import com.ciyocloud.itam.service.ModelsService;
import com.ciyocloud.itam.vo.ModelsVO;
import lombok.extern.slf4j.Slf4j;

/**
 * 型号SSE进度导入监听器
 *
 * @author codeck
 * @since 2026-01-30
 */
@Slf4j
public class ModelsImportListener extends SseProgressExcelListener<ModelsVO> {

    private final ModelsService modelsService;
    private final ManufacturersService manufacturersService;
    private final CategoriesService categoriesService;
    private final DepreciationsService depreciationsService;
    private final Long operatorId;

    public ModelsImportListener(String progressKey, Long userId) {
        super(progressKey, userId, false, 50, 1000);
        this.modelsService = SpringContextUtils.getBean(ModelsService.class);
        this.manufacturersService = SpringContextUtils.getBean(ManufacturersService.class);
        this.categoriesService = SpringContextUtils.getBean(CategoriesService.class);
        this.depreciationsService = SpringContextUtils.getBean(DepreciationsService.class);
        this.operatorId = userId;
    }

    @Override
    public void processData(ModelsVO model, AnalysisContext context) {
        int currentRow = context.readRowHolder().getRowIndex() + 1;

        // 验证必填字段
        if (StrUtil.isBlank(model.getName())) {
            throw new BusinessException(String.format("第%d行 型号名称不能为空", currentRow));
        }
        if (StrUtil.isBlank(model.getManufacturerName())) {
            throw new BusinessException(String.format("第%d行 厂商名称不能为空", currentRow));
        }
//        if (StrUtil.isBlank(model.getCategoryName())) {
//            throw new BusinessException(String.format("第%d行 分类名称不能为空", currentRow));
//        }

        // 根据厂商名称查找厂商ID
        ManufacturersEntity manufacturer = manufacturersService.getOneSafe(
                new LambdaQueryWrapper<ManufacturersEntity>()
                        .eq(ManufacturersEntity::getName, model.getManufacturerName())
        );
        if (manufacturer == null) {
            throw new BusinessException(String.format("第%d行 厂商 '%s' 不存在", currentRow, model.getManufacturerName()));
        }
        model.setManufacturerId(manufacturer.getId());

        // 根据分类名称查找分类ID
//        CategoriesEntity category = categoriesService.getOneSafe(
//                new LambdaQueryWrapper<CategoriesEntity>()
//                        .eq(CategoriesEntity::getName, model.getCategoryName())
//        );
//        if (category == null) {
//            throw new BusinessException(String.format("第%d行 分类 '%s' 不存在", currentRow, model.getCategoryName()));
//        }
//        model.setCategoryId(category.getId());

        // 根据折旧规则名称查找折旧规则ID（可选）
        if (StrUtil.isNotBlank(model.getDepreciationName())) {
            DepreciationsEntity depreciation = depreciationsService.getOneSafe(
                    new LambdaQueryWrapper<DepreciationsEntity>()
                            .eq(DepreciationsEntity::getName, model.getDepreciationName())
            );
            if (depreciation == null) {
                throw new BusinessException(String.format("第%d行 折旧规则 '%s' 不存在", currentRow, model.getDepreciationName()));
            }
            model.setDepreciationId(depreciation.getId());
        }

        // 检查同厂商下型号名称是否已存在
        ModelsEntity existing = modelsService.getOneSafe(
                new LambdaQueryWrapper<ModelsEntity>()
                        .eq(ModelsEntity::getName, model.getName())
                        .eq(ModelsEntity::getManufacturerId, model.getManufacturerId())
        );

        // 将 VO 转换为 Entity
        ModelsEntity entity = new ModelsEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setImageUrl(model.getImageUrl());
        entity.setManufacturerId(model.getManufacturerId());
        entity.setCategoryId(model.getCategoryId());
        entity.setDepreciationId(model.getDepreciationId());
        entity.setModelNumber(model.getModelNumber());
        entity.setEol(model.getEol());
        entity.setCategoryId(0L);
        if (ObjectUtil.isNull(existing)) {
            // 新增型号
            entity.setCreateBy(operatorId);
            entity.setDeleted(0);
            modelsService.save(entity);
        } else {
            // 根据导入数据是否有ID来决定是更新还是跳过
            if (model.getId() != null && model.getId().equals(existing.getId())) {
                // 更新型号
                entity.setUpdateBy(operatorId);
                // 保留创建信息
                entity.setCreateBy(existing.getCreateBy());
                entity.setCreateTime(existing.getCreateTime());
                entity.setDeleted(existing.getDeleted());
                modelsService.updateById(entity);
            } else {
                throw new BusinessException(String.format("厂商 '%s' 下型号 '%s' 已存在",
                    model.getManufacturerName(), model.getName()));
            }
        }
    }
}
