package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.itam.entity.CategoriesEntity;

import java.util.List;

/**
 * 分类Service接口
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
public interface CategoriesService extends IService<CategoriesEntity> {

    /**
     * 查询分类树
     *
     * @param categoriesEntity 查询条件
     * @return 树
     */
    List<CategoriesEntity> listTree(CategoriesEntity categoriesEntity);

    /**
     * 校验分类编码是否唯一
     *
     * @param categories 分类信息
     * @return 状态码
     */
    String checkCategoryCodeUnique(CategoriesEntity categories);
}
