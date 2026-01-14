package com.ciyocloud.api.web.controller.system;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.message.websocket.WsMessage;
import com.ciyocloud.message.websocket.WsSessionManager;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import com.ciyocloud.system.entity.SysAnnounceEntity;
import com.ciyocloud.system.entity.SysAnnounceSendEntity;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.service.SysAnnouncementSendService;
import com.ciyocloud.system.service.SysAnnouncementService;
import com.ciyocloud.system.service.SysUserService;
import com.ciyocloud.system.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 通知公告
 *
 * @author : codeck
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sys/announce")
public class SysAnnouncementController {
    private final SysAnnouncementService sysAnnouncementService;
    private final SysAnnouncementSendService sysAnnouncementSendService;
    private final SysUserService sysUserService;

    /**
     * 通知分页列表查询
     */
    @GetMapping(value = "/page")
    @SaCheckPermission("sys:announcement:query")
    public Result<IPage<SysAnnounceEntity>> queryPage(SysAnnounceEntity sysAnnouncement, Page page) {
        QueryWrapper<SysAnnounceEntity> queryWrapper = QueryWrapperUtils.toSimpleQuery(sysAnnouncement);
        queryWrapper.eq("msg_category", "1");
        queryWrapper.eq("del_flag", "0");
        return Result.success(sysAnnouncementService.page(page, queryWrapper));
    }

    /**
     * 添加
     */
    @PostMapping
    @SaCheckPermission("sys:announcement:save")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    public Result<SysAnnounceEntity> save(@RequestBody SysAnnounceEntity sysAnnouncement) {
        // 标题处理xss攻击的问题
        String title = HtmlUtil.filter(sysAnnouncement.getTitle());
        sysAnnouncement.setTitle(title);
        sysAnnouncement.setDelFlag("0");
        //未发布
        sysAnnouncement.setSender(SecurityUtils.getUsername());
        sysAnnouncement.setCreateBy(SecurityUtils.getUserId());
        sysAnnouncement.setSendStatus(SysAnnounceEntity.SendStatusEnum.NO_SEND);
        sysAnnouncementService.saveAnnouncement(sysAnnouncement);
        return Result.success();
    }

    /**
     * 编辑
     */
    @PutMapping
    @SaCheckPermission("sys:announcement:update")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    public Result<SysAnnounceEntity> update(@RequestBody SysAnnounceEntity sysAnnouncement) {
        //标题处理xss攻击的问题
        String title = HtmlUtil.filter(sysAnnouncement.getTitle());
        sysAnnouncement.setTitle(title);
        sysAnnouncement.setUpdateBy(SecurityUtils.getUserId());
        sysAnnouncementService.updateAnnouncement(sysAnnouncement);
        return Result.success();
    }

    /**
     * 通过id删除
     */
    @DeleteMapping("/{ids}")
    @SaCheckPermission("sys:announcement:delete")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    public Result<SysAnnounceEntity> delete(@PathVariable List<Long> ids) {
        List<SysAnnounceEntity> announcementList = ids.stream().map(id -> {
            SysAnnounceEntity announcement = new SysAnnounceEntity();
            announcement.setDelFlag("1");
            announcement.setId(id);
            return announcement;
        }).collect(Collectors.toList());
        sysAnnouncementService.updateBatchById(announcementList);
        return Result.success();
    }

    /**
     * 通过id查询
     */
    @GetMapping("/{id}")
    @SaCheckPermission("sys:announcement:query")
    public Result<SysAnnounceEntity> getInfo(@PathVariable(name = "id") Long id) {
        SysAnnounceEntity entity = sysAnnouncementService.getById(id);
        if (entity == null) {
            return Result.success(null);
        }

        // 转换为VO对象
        SysAnnounceVO vo = SystemEntityConvert.INSTANCE.convertSysAnnounce(entity);
        // 如果userIds有值，根据逗号拆分查询用户列表
        if (StrUtil.isNotBlank(entity.getUserIds())) {
            String[] userIdArray = StrUtil.splitToArray(entity.getUserIds(), ",");
            List<Long> userIds = Arrays.stream(userIdArray)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            // 查询用户信息
            List<SysUserEntity> userEntities = sysUserService.listByIds(userIds);
            List<SysUserVO> userList = userEntities.stream()
                    .map(SystemEntityConvert.INSTANCE::convertSysUser)
                    .collect(Collectors.toList());
            vo.setUserList(userList);
        }
        return Result.success(vo);
    }

    /**
     * 更新发布操作
     */
    @PostMapping("/release/{id}")
    @Log(title = "通知公告", businessType = BusinessType.OTHER)
    @SaCheckPermission("sys:announcement:release")
    public Result<SysAnnounceEntity> releaseAnnouncement(@PathVariable(name = "id", required = true) Long id) {
        SysAnnounceEntity sysAnnouncement = sysAnnouncementService.getById(id);
        if (sysAnnouncement != null) {
            sysAnnouncement.setSendStatus(SysAnnounceEntity.SendStatusEnum.HAS_SEND);
            sysAnnouncement.setSendTime(LocalDateTime.now());
            String currentUserName = SecurityUtils.getUsername();
            sysAnnouncement.setSender(currentUserName);
            boolean update = sysAnnouncementService.updateById(sysAnnouncement);
            if (update) {
                if (sysAnnouncement.getMsgType().equals(SysAnnounceEntity.MsgTypeEnum.ALL)) {
                    WsSessionManager.sendMessage(WsMessage.builder().msgType(WsMessage.MsgType.SYS_MSG).body(sysAnnouncement).build());
                } else {
                    String[] userIdList = StrUtil.splitToArray(sysAnnouncement.getUserIds(), ",");
                    WsSessionManager.sendMessage(userIdList, WsMessage.builder().msgType(WsMessage.MsgType.SYS_MSG).body(sysAnnouncement).build());
                }
            }
        }
        return Result.success();
    }

    /**
     * 更新撤销操作
     */
    @Log(title = "通知公告", businessType = BusinessType.OTHER)
    @PostMapping("/revoke/{id}")
    @SaCheckPermission("sys:announcement:revoke")
    public Result<SysAnnounceEntity> revokeAnnouncement(@PathVariable(name = "id", required = true) Long id) {
        SysAnnounceEntity sysAnnouncement = sysAnnouncementService.getById(id);
        if (sysAnnouncement != null) {
            //撤销发布
            sysAnnouncement.setSendStatus(SysAnnounceEntity.SendStatusEnum.HAS_CANCLE);
            sysAnnouncement.setCancelTime(LocalDateTime.now());
            boolean ok = sysAnnouncementService.updateById(sysAnnouncement);
        }
        return Result.successMsg("撤回消息成功");
    }


    /**
     * 通告查看详情页面
     * 直接跳转到详情页面
     */
    @GetMapping("/show/{id}")
    public ModelAndView showContent(ModelAndView modelAndView, @PathVariable("id") Long id) {
        SysAnnounceEntity announcement = sysAnnouncementService.getById(id);
        if (announcement != null) {
            modelAndView.addObject("data", announcement);
            modelAndView.setViewName("announce/showContent");
            return modelAndView;
        }
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }


    /**
     * 获取我未读的通知列表
     */
    @GetMapping("/unread/list")
    public Result<List<UnReadAnnouncementVO>> getUnReadAnnouncementList() {
        return sysAnnouncementSendService.getUnReadAnnouncementList();

    }

    /***
     *  通知列表 返回最新未读分类数据 查询接收人为全部的消息补全
     */
    @GetMapping("/notice")
    public Result<Map<String, Object>> getAnnounceNotice() {
        return sysAnnouncementSendService.getAnnounceNotice();
    }


    /**
     * 获取我的所有消息
     */
    @GetMapping(value = "/getMyAnnouncementSend")
    public Result<IPage<AnnouncementSendVO>> getMyAnnouncementSend(AnnouncementSendVO announcementSendModel, Page<AnnouncementSendVO> page) {
        Long userId = SecurityUtils.getUserId();
        announcementSendModel.setUserId(userId);
        Page<AnnouncementSendVO> myAnnouncementSendPage = sysAnnouncementSendService.getMyAnnouncementSendPage(page, announcementSendModel);
        return Result.success(myAnnouncementSendPage);
    }

    /**
     * 标记已读
     */
    @PutMapping(value = "/read/{id}")
    public Result<SysAnnounceSendEntity> read(@PathVariable("id") Long id) {
        SysAnnounceSendEntity announcementSend = new SysAnnounceSendEntity();
        announcementSend.setReadTime(LocalDateTime.now());
        announcementSend.setReadFlag("1");
        sysAnnouncementSendService.update(announcementSend, Wrappers.<SysAnnounceSendEntity>lambdaUpdate().eq(SysAnnounceSendEntity::getAnntId, id).eq(SysAnnounceSendEntity::getUserId, SecurityUtils.getUserId()));
        return Result.success();
    }


    /**
     * 全部已读
     */
    @PutMapping(value = "/readAll")
    public Result<SysAnnounceSendEntity> readAll() {
        SysAnnounceSendEntity announcementSend = new SysAnnounceSendEntity();
        announcementSend.setReadTime(LocalDateTime.now());
        announcementSend.setReadFlag("1");
        sysAnnouncementSendService.update(announcementSend, Wrappers.<SysAnnounceSendEntity>lambdaUpdate().eq(SysAnnounceSendEntity::getUserId, SecurityUtils.getUserId()));
        return Result.success();
    }


}
