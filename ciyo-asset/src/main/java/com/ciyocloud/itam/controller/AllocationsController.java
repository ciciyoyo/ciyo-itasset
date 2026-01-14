package com.ciyocloud.itam.controller;

import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.AllocationsEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.req.AllocationsPageReq;
import com.ciyocloud.itam.service.AllocationsService;
import com.ciyocloud.itam.util.AssetPermissionUtils;
import com.ciyocloud.itam.vo.AllocationsVO;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源分配/领用明细表
 *
 * @author codeck
 * @since 2025-12-30
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/allocations")
public class AllocationsController {

    private final AllocationsService allocationsService;
    private final AssetPermissionUtils assetPermissionUtils;


    /**
     * 分页查询资源分配/领用明细表列表 (VO)
     */
    @GetMapping("/page")
    public Result<PageResultVO<AllocationsVO>> queryPageVo(PageRequest page, AllocationsPageReq req) {
        assetPermissionUtils.hasAssetPermi(req.getItemType().getCode(), "page");
        return Result.success(new PageResultVO<>(allocationsService.queryPageVo(page.toMpPage(), req)));
    }

    /**
     * 导出资源分配/领用明细表列表
     */
    @Log(title = "分配明细表", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(AllocationsPageReq req) {
        assetPermissionUtils.hasAssetPermi(req.getItemType().getCode(), "export");
        List<AllocationsVO> list = allocationsService.queryListVo(req);
        ExcelUtils.exportExcel(list, "分配明细表数据", AllocationsVO.class);
    }

    /**
     * 资源分配
     */
    @Log(title = "资源分配", businessType = BusinessType.INSERT)
    @PostMapping("/allocate")
    public Result<Boolean> allocate(@RequestBody AllocationsEntity allocations) {
        ValidatorUtils.validateEntity(allocations, AddGroup.class);
        return Result.success(allocationsService.allocate(allocations));
    }

    /**
     * 资源取消分配 (归还)
     *
     * @param id        数据ID
     * @param assetType 资产类型
     */
    @Log(title = "资源取消分配", businessType = BusinessType.UPDATE)
    @PostMapping("/deallocate/{id}")
    public Result<Boolean> deallocate(@PathVariable Long id, @RequestParam AssetType assetType) {
        assetPermissionUtils.hasAssetPermi(assetType.getCode(), "update");
        return Result.success(allocationsService.deallocate(id));
    }

    /**
     * 删除资源分配/领用明细表
     *
     * @param ids 主键串
     */
    @Log(title = "资源分配/领用明细表", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids, @RequestParam AssetType assetType) {
        assetPermissionUtils.hasAssetPermi(assetType.getCode(), "delete");
        return Result.success(allocationsService.removeByIds(ids));
    }


}
