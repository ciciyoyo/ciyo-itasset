package com.ciyocloud.itam.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.excel.core.SseProgressExcelListener;
import com.ciyocloud.itam.entity.CategoriesEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.service.CategoriesService;
import lombok.extern.slf4j.Slf4j;

/**
 * 分类SSE进度导入监听器
 *
 * @author codeck
 * @since 2026-01-30
 */
@Slf4j
public class CategoriesImportListener extends SseProgressExcelListener<CategoriesEntity> {

    private final CategoriesService categoriesService;
    private final Long operatorId;
    private final AssetType categoryType;

    public CategoriesImportListener(String progressKey, Long userId, AssetType categoryType) {
        super(progressKey, userId, false, 50, 1000);
        this.categoriesService = SpringContextUtils.getBean(CategoriesService.class);
        this.operatorId = userId;
        this.categoryType = categoryType;
    }

    @Override
    public void processData(CategoriesEntity category, AnalysisContext context) {
        int currentRow = context.readRowHolder().getRowIndex() + 1;

        // 验证必填字段
        if (StrUtil.isBlank(category.getName())) {
            throw new BusinessException(String.format("第%d行 分类名称不能为空", currentRow));
        }
        if (StrUtil.isBlank(category.getCode())) {
            throw new BusinessException(String.format("第%d行 分类编码不能为空", currentRow));
        }

        // 从导入参数设置分类大类
        if (null != categoryType) {
            category.setCategoryType(categoryType);
        }

        if (category.getCategoryType() == null) {
            throw new BusinessException(String.format("第%d行 分类大类不能为空", currentRow));
        }

        // 根据父级分类名称查找父级ID
        if (StrUtil.isNotBlank(category.getParentName())) {
            CategoriesEntity parent = categoriesService.getOneSafe(
                    new LambdaQueryWrapper<CategoriesEntity>()
                            .eq(CategoriesEntity::getName, category.getParentName())
                            .eq(CategoriesEntity::getCategoryType, category.getCategoryType())
            );
            if (parent == null) {
                throw new BusinessException(String.format("第%d行 父级分类 '%s' 不存在", currentRow, category.getParentName()));
            }
            category.setParentId(parent.getId());
        } else {
            // 没有父级名称，设为顶级节点
            category.setParentId(null);
        }

        // 检查分类编码是否已存在（编码全局唯一）
        CategoriesEntity existingByCode = categoriesService.getOneSafe(
                new LambdaQueryWrapper<CategoriesEntity>()
                        .eq(CategoriesEntity::getCode, category.getCode())
        );

        if (ObjectUtil.isNotNull(existingByCode)) {
            // 如果导入数据有ID且与现有记录匹配，则更新
            if (category.getId() != null && category.getId().equals(existingByCode.getId())) {
                // 更新分类
                category.setUpdateBy(operatorId);
                // 保留创建信息
                category.setCreateBy(existingByCode.getCreateBy());
                category.setCreateTime(existingByCode.getCreateTime());
                category.setDeleted(existingByCode.getDeleted());
                categoriesService.updateById(category);
            } else {
                throw new BusinessException(String.format("第%d行 分类编码 '%s' 已存在", currentRow, category.getCode()));
            }
        } else {
            // 新增分类
            category.setCreateBy(operatorId);
            category.setDeleted(0);
            categoriesService.save(category);
        }
    }
}
