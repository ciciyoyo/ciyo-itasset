package com.ciyocloud.itam.service.impl;

import com.ciyocloud.common.mybatis.service.BaseServiceImpl;
import com.ciyocloud.common.sse.util.SseAsyncProcessUtils;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.CategoriesEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.listener.CategoriesImportListener;
import com.ciyocloud.itam.mapper.CategoriesMapper;
import com.ciyocloud.itam.service.CategoriesService;
import com.ciyocloud.system.constant.UserConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 分类Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
@Slf4j
@Service
public class CategoriesServiceImpl extends BaseServiceImpl<CategoriesMapper, CategoriesEntity> implements CategoriesService {

    @Override
    public List<CategoriesEntity> listTree(CategoriesEntity categoriesEntity) {
        List<CategoriesEntity> list = this.list(QueryWrapperUtils.toSimpleQuery(categoriesEntity));
        return buildTree(list);
    }

    private List<CategoriesEntity> buildTree(List<CategoriesEntity> list) {
        List<CategoriesEntity> returnList = new ArrayList<>();
        List<Long> tempList = new ArrayList<>();
        for (CategoriesEntity entity : list) {
            tempList.add(entity.getId());
        }
        for (CategoriesEntity entity : list) {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(entity.getParentId())) {
                recursionFn(list, entity);
                returnList.add(entity);
            }
        }
        if (returnList.isEmpty()) {
            returnList = list;
        }
        return returnList;
    }

    private void recursionFn(List<CategoriesEntity> list, CategoriesEntity t) {
        // 得到子节点列表
        List<CategoriesEntity> childList = getChildList(list, t);
        t.setChildren(childList);
        for (CategoriesEntity tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    private List<CategoriesEntity> getChildList(List<CategoriesEntity> list, CategoriesEntity t) {
        List<CategoriesEntity> tlist = new ArrayList<>();
        Iterator<CategoriesEntity> it = list.iterator();
        while (it.hasNext()) {
            CategoriesEntity n = (CategoriesEntity) it.next();
            if (n.getParentId() != null && n.getParentId().longValue() == t.getId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    private boolean hasChild(List<CategoriesEntity> list, CategoriesEntity t) {
        return getChildList(list, t).size() > 0;
    }

    @Override
    public String checkCategoryCodeUnique(CategoriesEntity categories) {
        Long id = categories.getId() == null ? -1L : categories.getId();
        CategoriesEntity info = this.lambdaQuery()
                .eq(CategoriesEntity::getCode, categories.getCode())
                .one();
        if (info != null && !info.getId().equals(id)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public void importData(InputStream inputStream, String originalFilename, String progressKey, Long userId, AssetType categoryType) {
        try {
            log.info("开始导入分类数据，progressKey: {}, userId: {}, 文件名: {}, 分类类型: {}", progressKey, userId, originalFilename, categoryType);

            // 初始化进度
            SseAsyncProcessUtils.setTips("正在解析Excel文件...");

            // 创建支持SSE进度推送的导入监听器
            CategoriesImportListener listener = new CategoriesImportListener(progressKey, userId, categoryType);
            // 更新进度：开始导入
            SseAsyncProcessUtils.setProcess(0, "开始导入分类数据...");
            //执行导入
            ExcelUtils.importExcel(inputStream, CategoriesEntity.class, listener);
        } catch (Exception e) {
            log.error("分类数据导入失败，progressKey: {}, 错误: {}", progressKey, e.getMessage(), e);
            SseAsyncProcessUtils.setError("导入失败: " + e.getMessage());
            throw e;
        }
    }
}
