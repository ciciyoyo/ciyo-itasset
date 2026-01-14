package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.itam.entity.AccessoriesEntity;
import com.ciyocloud.itam.vo.AccessoriesVO;

import java.util.List;
import java.util.Map;

/**
 * 配件Service接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:26
 */
public interface AccessoriesService extends IService<AccessoriesEntity> {

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

}
