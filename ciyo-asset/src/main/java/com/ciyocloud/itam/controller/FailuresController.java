package com.ciyocloud.itam.controller;

import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.itam.entity.FailuresEntity;
import com.ciyocloud.itam.req.FailuresPageReq;
import com.ciyocloud.itam.service.FailuresService;
import com.ciyocloud.itam.vo.FailuresVO;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 故障表
 *
 * @author codeck
 * @since 2026-01-01
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/failures")
public class FailuresController {

    private final FailuresService failuresService;

    /**
     * 查询故障列表
     */
    @PreAuthorize("@ss.hasPermi('itam:failures:page')")
    @GetMapping("/page")
    public Result<PageResultVO<FailuresVO>> queryPage(PageRequest page, FailuresPageReq req) {
        return Result.success(new PageResultVO<>(failuresService.queryPageVo(page.toMpPage(), req)));
    }

    /**
     * 报告故障
     */
    @Log(title = "故障", businessType = BusinessType.INSERT)
    @PostMapping("/report")
    public Result<Boolean> report(@RequestBody FailuresEntity failures) {
        ValidatorUtils.validateEntity(failures, AddGroup.class);
        failuresService.reportFailure(failures);
        return Result.success(true);
    }

    /**
     * 解决故障
     */
    @Log(title = "故障", businessType = BusinessType.UPDATE)
    @PostMapping("/resolve")
    public Result<Boolean> resolve(@RequestBody FailuresEntity failures) {
        ValidatorUtils.validateEntity(failures, UpdateGroup.class);
        failuresService.resolveFailure(failures.getId(), failures.getNotes());
        return Result.success(true);
    }


    /**
     * 删除故障
     *
     * @param ids 主键串
     */
    @Log(title = "故障", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(failuresService.removeByIds(ids));
    }
}
