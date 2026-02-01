package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.mybatis.service.BaseService;
import com.ciyocloud.itam.entity.FailuresEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.req.FailuresPageReq;
import com.ciyocloud.itam.req.FailuresReportReq;
import com.ciyocloud.itam.vo.FailuresVO;

/**
 * 故障表Service接口
 *
 * @author codeck
 * @since 2026-01-01
 */
public interface FailuresService extends BaseService<FailuresEntity> {

    /**
     * 报告故障
     */
    void reportFailure(FailuresEntity failure);

    /**
     * 报告故障 (简化参数)
     */
    void reportFailure(AssetType targetType, FailuresReportReq req);

    /**
     * 解决故障
     */
    void resolveFailure(Long id, String notes);

    /**
     * 分页查询故障列表
     *
     * @param page 分页对象
     * @param req  查询参数
     * @return 分页结果
     */
    Page<FailuresVO> queryPageVo(Page<FailuresVO> page, FailuresPageReq req);
}
