package com.ciyocloud.api.web.controller.system;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.system.entity.SysDeptPostEntity;
import com.ciyocloud.system.entity.SysPostEntity;
import com.ciyocloud.system.request.DeptPostRequest;
import com.ciyocloud.system.service.SysDeptPostService;
import com.ciyocloud.system.service.SysPostService;
import com.ciyocloud.system.vo.SysDeptPostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门岗位关系
 *
 * @author codeck-gen
 * @since 2022-06-06 16:25:14
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dept/post")
public class SysDeptPostController {
    private final SysDeptPostService sysDeptPostService;

    private final SysPostService sysPostService;

    /**
     * 查询部门岗位关系列表
     */
    @PreAuthorize("@ss.hasPermi('system:deptpost:list')")
    @GetMapping("/page")
    public Result queryPage(Page page, SysDeptPostEntity sysDeptPost) {
        return Result.success(sysDeptPostService.page(page, sysDeptPost));
    }


    /**
     * 查询部门下不存在的岗位列表
     */
    @PreAuthorize("@ss.hasPermi('system:deptpost:setting')")
    @GetMapping("/queryDeptNotInPost")
    public Result queryDeptNotInPostPage(Page page, SysDeptPostVO sysDeptPost) {
        List<SysDeptPostEntity> list = sysDeptPostService.list(Wrappers.<SysDeptPostEntity>lambdaQuery().eq(SysDeptPostEntity::getDeptId, sysDeptPost.getDeptId()));
        return Result.success(sysPostService.page(page, Wrappers.<SysPostEntity>lambdaQuery()
                .eq(SysPostEntity::getStatus, 0)
                .like(StrUtil.isNotEmpty(sysDeptPost.getPostShowName()), SysPostEntity::getPostName, sysDeptPost.getPostShowName())
                .notIn(CollUtil.isNotEmpty(list), SysPostEntity::getId, list.stream().map(SysDeptPostEntity::getPostId).collect(Collectors.toList()))));
    }


    /**
     * 查询部门下存在的岗位列表
     */
    @GetMapping("/queryDeptInPost")
    public Result queryDeptInPost(SysDeptPostEntity sysDeptPost) {
        if (null == sysDeptPost.getDeptId()) {
            return Result.success();
        }
        List<SysDeptPostEntity> list = sysDeptPostService.list(Wrappers.<SysDeptPostEntity>lambdaQuery().eq(SysDeptPostEntity::getDeptId, sysDeptPost.getDeptId()));
        return Result.success(list);
    }


    /**
     * 修改岗位部门关系
     */
    @PreAuthorize("@ss.hasPermi('system:deptpost:update')")
    @PostMapping("/update")
    public Result update(@RequestBody SysDeptPostEntity sysDeptPost) {
        sysDeptPostService.updateById(sysDeptPost);
        return Result.success();
    }


    /**
     * 设置部门岗位
     */
    @PreAuthorize("@ss.hasPermi('system:deptpost:setting')")
    @PostMapping("/settingDeptPost")
    public Result settingDeptPost(@RequestBody DeptPostRequest.Setting settingRequest) {
        if (CollUtil.isEmpty(settingRequest.getIds())) {
            return Result.failed("请选择岗位");
        }
        List<SysPostEntity> postEntityList = sysPostService.list(Wrappers.<SysPostEntity>lambdaQuery().in(SysPostEntity::getId, settingRequest.getIds()));
        List<SysDeptPostEntity> list = postEntityList.stream().map(post -> {
            SysDeptPostEntity sysDeptPost = new SysDeptPostEntity();
            sysDeptPost.setPostId(post.getId());
            sysDeptPost.setDeptId(settingRequest.getDeptId());
            sysDeptPost.setPostShowName(post.getPostName());
            sysDeptPost.setUpdateTime(LocalDateTime.now());
            sysDeptPost.setCreateTime(LocalDateTime.now());
            return sysDeptPost;
        }).collect(Collectors.toList());
        sysDeptPostService.saveBatch(list);
        return Result.success();
    }

    /**
     * 删除部门岗位关系
     */
    @PreAuthorize("@ss.hasPermi('system:deptpost:delete')")
    @DeleteMapping("/{ids}")
    public Result delete(@PathVariable List<Long> ids) {
        return Result.success(sysDeptPostService.removeByIds(ids));
    }
}
