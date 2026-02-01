package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.mybatis.service.BaseService;
import com.ciyocloud.itam.entity.AssetRequestsEntity;
import com.ciyocloud.itam.req.AssetRequestsApprovalReq;
import com.ciyocloud.itam.req.AssetRequestsPageReq;
import com.ciyocloud.itam.req.AssetRequestsSubmitReq;
import com.ciyocloud.itam.vo.AssetRequestsVO;

/**
 * 资产申请服务类
 *
 * @author codeck
 * @since 2026-01-10
 */
public interface AssetRequestsService extends BaseService<AssetRequestsEntity> {

    /**
     * 分页查询申请
     *
     * @param page
     * @param req  查询参数
     * @return 分页结果
     */
    Page<AssetRequestsVO> queryPage(PageRequest page, AssetRequestsPageReq req);

    /**
     * 提交申请
     *
     * @param req 提交参数
     */
    void submitRequest(AssetRequestsSubmitReq req);

    /**
     * 审批申请
     *
     * @param req 审批参数
     */
    void approveRequest(AssetRequestsApprovalReq req);

    /**
     * 管理端分页查询申请
     */
    Page<AssetRequestsVO> queryManagePage(PageRequest page, AssetRequestsPageReq req);
}
