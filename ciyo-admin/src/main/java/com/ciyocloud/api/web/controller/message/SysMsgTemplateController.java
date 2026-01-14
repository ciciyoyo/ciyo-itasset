package com.ciyocloud.api.web.controller.message;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.JsonUtils;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.message.entity.SysMsgTemplateEntity;
import com.ciyocloud.message.entity.TestMsgParams;
import com.ciyocloud.message.service.SysMsgTemplateService;
import com.ciyocloud.message.util.PushMsgUtils;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息模板管理
 *
 * @author codeck-gen
 * @since 2021-12-28 10:26:51
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/sys/msg/template")
public class SysMsgTemplateController {
    private final SysMsgTemplateService sysMsgTemplateService;
    private final PushMsgUtils pushMsgUtils;

    /**
     * 查询消息列表
     */
    @SaCheckPermission("sys:msgtemplate:query")
    @GetMapping("/page")
    public Result queryPage(Page page, SysMsgTemplateEntity sysMsgTemplate) {
        return Result.success(sysMsgTemplateService.page(page, QueryWrapperUtils.toSimpleQuery(sysMsgTemplate)));
    }


    /**
     * 查询全部
     *
     * @param sysMsgTemplate
     * @return
     */
    @GetMapping("/list")
    public Result list(SysMsgTemplateEntity sysMsgTemplate) {
        return Result.success(sysMsgTemplateService.list(QueryWrapperUtils.toSimpleQuery(sysMsgTemplate)));
    }


    /**
     * 获取消息详细信息
     */
    @SaCheckPermission("sys:msgtemplate:query")
    @GetMapping(value = "/{id}")
    public Result getInfo(@PathVariable("id") Long id) {
        return Result.success(sysMsgTemplateService.getById(id));
    }

    /**
     * 新增消息
     */
    @SaCheckPermission("sys:msgtemplate:save")
    @Log(title = "消息通知模板", businessType = BusinessType.INSERT)
    @PostMapping
    public Result save(@RequestBody SysMsgTemplateEntity sysMsgTemplate) {
        return Result.success(sysMsgTemplateService.saveMsgTemplate(sysMsgTemplate));
    }

    /**
     * 修改消息
     */
    @SaCheckPermission("sys:msgtemplate:update")
    @Log(title = "消息通知模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result update(@RequestBody SysMsgTemplateEntity sysMsgTemplate) {
        return Result.success(sysMsgTemplateService.updateMsgTemplate(sysMsgTemplate));
    }

    /**
     * 删除消息
     */
    @SaCheckPermission("sys:msgtemplate:delete")
    @Log(title = "消息通知模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public Result delete(@PathVariable List<String> ids) {
        return Result.success(sysMsgTemplateService.removeByIds(ids));
    }

    /**
     * 发送消息
     */
    @PostMapping(value = "/sendMsg")
    @Log(title = "消息通知模板发送", businessType = BusinessType.OTHER)
    public Result<SysMsgTemplateEntity> sendMessage(@RequestBody TestMsgParams msgParams) {
        String testData = msgParams.getTestData();
        Map<String, Object> params = null;
        if (StrUtil.isNotBlank(testData)) {
            params = JsonUtils.jsonToMap(testData);
        }
        pushMsgUtils.sendMessageByTemplateCode(msgParams.getMsgType(), msgParams.getTemplateCode(), params, msgParams.getReceiver());
        return Result.success();
    }

}
