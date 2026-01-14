package com.ciyocloud.api.web.controller.message;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.message.entity.SysMessageEntity;
import com.ciyocloud.message.service.SysMessageService;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息管理
 *
 * @author codeck
 * @since 2021-12-27 17:41:10
 */
@RestController
@RequestMapping("/sys/msg")
@RequiredArgsConstructor
public class SysMessageController {
    private final SysMessageService sysMessageService;

    /**
     * 查询消息列表
     */
    @SaCheckPermission("sys:msg:page")
    @GetMapping("/page")
    public Result queryPage(Page page, SysMessageEntity sysMessage) {
        return Result.success(sysMessageService.page(page, QueryWrapperUtils.toSimpleQuery(sysMessage)));
    }


    /**
     * 获取消息详细信息
     */
    @SaCheckPermission("sys:msg:query")
    @GetMapping(value = "/{id}")
    public Result getInfo(@PathVariable("id") Long id) {
        return Result.success(sysMessageService.getById(id));
    }


    /**
     * 删除消息
     */
    @SaCheckPermission("sys:msg:delete")
    @Log(title = "消息日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public Result delete(@PathVariable List<Long> ids) {
        return Result.success(sysMessageService.removeByIds(ids));
    }
}
