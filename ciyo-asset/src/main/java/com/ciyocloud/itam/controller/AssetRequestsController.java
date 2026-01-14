package com.ciyocloud.itam.controller;

import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.itam.req.AssetRequestsApprovalReq;
import com.ciyocloud.itam.req.AssetRequestsPageReq;
import com.ciyocloud.itam.req.AssetRequestsSubmitReq;
import com.ciyocloud.itam.service.IAssetRequestsService;
import com.ciyocloud.itam.vo.AssetRequestsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 资产申请 前端控制器
 *
 * @author codeck
 * @since 2026-01-10
 */
@RestController
@RequestMapping("/itam/asset-requests")
@RequiredArgsConstructor
public class AssetRequestsController {

    private final IAssetRequestsService assetRequestsService;

    /**
     * 分页查询申请
     */
    @GetMapping("/page")
    public Result<PageResultVO<AssetRequestsVO>> page(PageRequest page, AssetRequestsPageReq req) {
        return Result.success(new PageResultVO<>(assetRequestsService.queryPage(page, req)));
    }

    /**
     * 提交申请
     */
    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody @Validated AssetRequestsSubmitReq req) {
        assetRequestsService.submitRequest(req);
        return Result.success();
    }

    /**
     * 审批申请
     */
    @PostMapping("/approve")
    public Result<Void> approve(@RequestBody @Validated AssetRequestsApprovalReq req) {
        assetRequestsService.approveRequest(req);
        return Result.success();
    }
}
