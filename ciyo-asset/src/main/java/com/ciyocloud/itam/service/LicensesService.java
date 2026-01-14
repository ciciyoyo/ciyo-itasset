package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.itam.entity.LicensesEntity;
import com.ciyocloud.itam.req.LicenseAllocationReq;
import com.ciyocloud.itam.req.LicensePageReq;
import com.ciyocloud.itam.vo.LicenseAllocationVO;
import com.ciyocloud.itam.vo.LicensesVO;

import java.util.List;
import java.util.Map;


/**
 * 软件授权Service接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface LicensesService extends IService<LicensesEntity> {

    /**
     * 查询软件授权列表VO
     *
     * @param page 分页对象
     * @param req  查询条件
     * @return 软件授权VO分页对象
     */
    Page<LicensesVO> queryPageVo(Page<LicensesEntity> page, LicensePageReq req);

    /**
     * 查询软件授权列表VO (不分页)
     *
     * @param req 查询条件
     * @return 软件授权VO列表
     */
    List<LicensesVO> queryListVo(LicensePageReq req);

    /**
     * 分配软件授权
     *
     * @param req 分配请求
     * @return 是否成功
     */
    boolean allocate(LicenseAllocationReq req);

    /**
     * 查询软件分配列表
     *
     * @param page      分页对象
     * @param licenseId 软件授权ID
     * @return 分配VO分页对象
     */
    Page<LicenseAllocationVO> queryAllocations(Page<LicenseAllocationVO> page, Long licenseId);

    /**
     * 解除软件授权分配
     *
     * @param allocationId 分配记录ID
     * @return 是否成功
     */
    boolean deallocate(Long allocationId);


    /**
     * 获取授权分类统计
     *
     * @return 统计数据
     */
    List<Map<String, Object>> getCategoryStats();

    /**
     * 获取指标统计 (总数, 即将过期, 已过期, 库存不足)
     *
     * @return 统计数据
     */
    Map<String, Object> getIndicatorStats();

    /**
     * 批量删除软件授权
     *
     * @param ids ID列表
     * @return 结果
     */
    boolean removeLicensesByIds(List<Long> ids);
}

