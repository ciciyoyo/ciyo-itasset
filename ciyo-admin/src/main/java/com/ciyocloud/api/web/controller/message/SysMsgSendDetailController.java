package com.ciyocloud.api.web.controller.message;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.message.entity.SysMessageEntity;
import com.ciyocloud.message.entity.SysMsgSendDetailEntity;
import com.ciyocloud.message.service.SysMessageService;
import com.ciyocloud.message.service.SysMsgSendDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 发送操作记录
 *
 * @author codeck
 * @since 2023-12-23 21:11:06
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/sys/msg/sendDetail")
public class SysMsgSendDetailController {

    private final SysMsgSendDetailService sysMsgSendDetailService;
    private final SysMessageService sysMessageService;

    /**
     * 查询发送操作记录列表
     */
    @GetMapping("/page")
    public Result querySendDetailPage(Page page, SysMsgSendDetailEntity sysMsgSendDetail) {
        return Result.success(sysMsgSendDetailService.page(page, QueryWrapperUtils.toSimpleQuery(sysMsgSendDetail)));
    }

    /**
     * 查看发送详情
     */
    @GetMapping("/{id}/page")
    public Result queryMsgPage(Page page, @PathVariable("id") Long id, String receiver) {
        return Result.success(sysMessageService.page(page, Wrappers.<SysMessageEntity>lambdaQuery()
                .eq(SysMessageEntity::getSendDetailId, id)
                .like(StrUtil.isNotBlank(receiver), SysMessageEntity::getReceiver, receiver)));
    }

}
