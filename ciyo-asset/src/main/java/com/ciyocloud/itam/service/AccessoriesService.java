package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.mybatis.service.BaseService;
import com.ciyocloud.itam.entity.AccessoriesEntity;
import com.ciyocloud.itam.req.AccessoriesPageReq;
import com.ciyocloud.itam.vo.AccessoriesVO;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 配件Service接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:26
 */
public interface AccessoriesService extends BaseService<AccessoriesEntity> {

    /**
     * 分页查询配件列表
     *
     * @param page 分页参数
     * @param req  请求参数
     * @return 结果分页
     */
    Page<AccessoriesVO> queryPage(PageRequest page, AccessoriesPageReq req);

    Page<AccessoriesVO> queryPageVo(Page<AccessoriesEntity> page, Wrapper<AccessoriesEntity> queryWrapper);

    /**
     * 查询配件列表 (VO)
     *
     * @param queryWrapper 查询条件
     * @return 结果列表
     */
    List<AccessoriesVO> queryListVo(Wrapper<AccessoriesEntity> queryWrapper);

    /**
     * 批量删除配件
     *
     * @param ids ID列表
     * @return 结果
     */
    boolean removeAccessoriesByIds(List<Long> ids);

    /**
     * 统计配件汇总数据
     *
     * @return 统计结果
     */
    Map<String, Object> getSummaryStats();

    /**
     * 导入配件数据
     *
     * @param inputStream      文件输入流
     * @param originalFilename 原始文件名
     * @param progressKey      前端传递的进度监听key
     * @param userId           用户 id
     */
    void importData(InputStream inputStream, String originalFilename, String progressKey, Long userId);

}
