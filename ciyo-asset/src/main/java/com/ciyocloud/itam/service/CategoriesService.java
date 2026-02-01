package com.ciyocloud.itam.service;

import com.ciyocloud.common.mybatis.service.BaseService;
import com.ciyocloud.itam.entity.CategoriesEntity;
import com.ciyocloud.itam.enums.AssetType;

import java.io.InputStream;
import java.util.List;

/**
 * 分类Service接口
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
public interface CategoriesService extends BaseService<CategoriesEntity> {

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

    /**
     * 导入分类数据
     *
     * @param inputStream      文件输入流
     * @param originalFilename 原始文件名
     * @param progressKey      前端传递的进度监听key
     * @param userId           用户 id
     */
    void importData(InputStream inputStream, String originalFilename, String progressKey, Long userId, AssetType categoryType);
}
