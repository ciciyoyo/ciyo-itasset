package com.ciyocloud.itam.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ciyocloud.common.entity.request.IdListRequest;
import com.ciyocloud.common.entity.request.PageRequest;
import com.ciyocloud.common.entity.vo.PageResultVO;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.common.validator.ValidatorUtils;
import com.ciyocloud.common.validator.group.AddGroup;
import com.ciyocloud.common.validator.group.UpdateGroup;
import com.ciyocloud.excel.util.ExcelUtils;
import com.ciyocloud.itam.entity.AllocationsEntity;
import com.ciyocloud.itam.entity.DeviceEntity;
import com.ciyocloud.itam.entity.FailuresEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.enums.DeviceStatus;
import com.ciyocloud.itam.req.AllocationsPageReq;
import com.ciyocloud.itam.req.DevicePageReq;
import com.ciyocloud.itam.service.*;
import com.ciyocloud.itam.vo.AllocationsVO;
import com.ciyocloud.itam.vo.AssetsReportVO;
import com.ciyocloud.itam.vo.DeviceDetailVO;
import com.ciyocloud.itam.vo.DeviceVO;
import com.ciyocloud.oplog.annotation.Log;
import com.ciyocloud.oplog.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 设备管理
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/device")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceDetailService deviceDetailService;
    private final FailuresService failuresService;
    private final AllocationsService allocationsService;
    private final AssetsReportService assetsReportService;
    private final DevicePrintService devicePrintService;

    /**
     * 查询设备列表
     */
    @SaCheckPermission("itam:device:page")
    @GetMapping("/page")
    public Result<PageResultVO<DeviceVO>> queryPage(PageRequest page, DevicePageReq req) {
        return Result.success(new PageResultVO<>(deviceService.queryPageVo(page.toMpPage(), req)));
    }

    /**
     * 查询设备列表
     */
    @SaCheckPermission("itam:device:page")
    @GetMapping("/list")
    public Result<List<DeviceEntity>> queryList(DevicePageReq req) {
        LambdaQueryWrapper<DeviceEntity> queryWrapper = Wrappers.<DeviceEntity>lambdaQuery();
        queryWrapper.like(StrUtil.isNotBlank(req.getAssetTag()), DeviceEntity::getName, req.getName());
        return Result.success(deviceService.list(queryWrapper));
    }

    /**
     * 导出设备列表
     */
    @SaCheckPermission("itam:device:export")
    @Log(title = "设备管理", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(DevicePageReq req) {
        List<DeviceVO> list = deviceService.queryListVo(req);
        ExcelUtils.exportExcel(list, "设备数据", DeviceVO.class);
    }

    /**
     * 导入设备
     */
    @SaCheckPermission("itam:device:import")
    @Log(title = "设备管理", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public Result<String> importData(MultipartFile file, @RequestParam String progressKey) throws Exception {
        Long userId = SecurityUtils.getUserId();
        ThreadUtil.execute(() -> {
            // 异步导入会删除文件 这里要转换到新的流
            try (var inputStream = new ByteArrayInputStream(file.getBytes())) {
                deviceService.importDevices(progressKey, inputStream, userId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return Result.success();
    }

    /**
     * 获取设备详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("itam:device:query")
    @GetMapping(value = "/{id:\\d+}")
    public Result<DeviceEntity> getInfo(@PathVariable Long id) {
        return Result.success(deviceService.getById(id));
    }

    /**
     * 新增设备
     */
    @SaCheckPermission("itam:device:add")
    @Log(title = "设备管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result<Boolean> add(@Validated(AddGroup.class) @RequestBody DeviceEntity device) {
        ValidatorUtils.validateEntity(device, AddGroup.class);
        device.setAssetsStatus(DeviceStatus.PENDING);
        return Result.success(deviceService.save(device));
    }

    /**
     * 修改设备
     */
    @SaCheckPermission("itam:device:update")
    @Log(title = "设备管理", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody DeviceEntity device) {
        ValidatorUtils.validateEntity(device, UpdateGroup.class);
        return Result.success(deviceService.updateById(device));
    }

    /**
     * 删除设备
     *
     * @param ids 主键串
     */
    @SaCheckPermission("itam:device:delete")
    @Log(title = "设备管理", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{ids}")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(deviceService.removeDevicesByIds(ids));
    }

    /**
     * 报告故障
     */
    @SaCheckPermission("itam:assets:report")
    @Log(title = "故障", businessType = BusinessType.INSERT)
    @PostMapping("/report")
    public Result<Boolean> report(@RequestBody FailuresEntity failures) {
        failuresService.reportFailure(failures);
        return Result.success(true);
    }

    /**
     * 设备报废
     */
    @SaCheckPermission("itam:device:scrap")
    @Log(title = "设备管理", businessType = BusinessType.UPDATE)
    @PostMapping("/scrap")
    public Result<Boolean> scrap(@RequestBody IdListRequest request) {
        return Result.success(deviceService.scrap(request.getIds()));
    }

    /**
     * 解决故障
     */
    @SaCheckPermission("itam:assets:resolve")
    @Log(title = "故障", businessType = BusinessType.UPDATE)
    @PostMapping("/resolve")
    public Result<Boolean> resolve(@RequestBody FailuresEntity failures) {
        failuresService.resolveFailure(failures.getId(), failures.getNotes());
        return Result.success(true);
    }


    /**
     * 分配配件
     */
    @SaCheckPermission("itam:accessories:update")
    @Log(title = "配件", businessType = BusinessType.UPDATE)
    @PostMapping("/allocate")
    public Result<Boolean> allocate(@RequestBody AllocationsEntity allocations) {
        allocations.setItemType(AssetType.DEVICE);
        return Result.success(allocationsService.allocate(allocations));
    }

    /**
     * 取消分配配件
     */
    @SaCheckPermission("itam:accessories:update")
    @Log(title = "配件", businessType = BusinessType.UPDATE)
    @PostMapping("/deallocate/{id}")
    public Result<Boolean> deallocate(@PathVariable Long id) {
        return Result.success(allocationsService.deallocate(id));
    }

    /**
     * 查询配件分配列表
     */
    @SaCheckPermission("itam:accessories:page")
    @GetMapping("/allocation/page")
    public Result<PageResultVO<AllocationsVO>> queryAllocationPage(PageRequest page, AllocationsPageReq req) {
        if (req == null) {
            req = new AllocationsPageReq();
        }
        req.setItemType(AssetType.DEVICE);
        return Result.success(new PageResultVO<>(allocationsService.queryPageVo(page.toMpPage(), req)));
    }

    /**
     * 获取当年内每月设备价值的趋势
     */
    @GetMapping("/stats/monthly-value")
    @SaCheckPermission("itam:accessories:page")
    public Result<List<AssetsReportVO>> getMonthlyValueStats() {
        return Result.success(assetsReportService.getMonthlyTrend(AssetType.DEVICE));
    }

    /**
     * 获取设备汇总统计数据
     */
    @GetMapping("/stats/summary")
    public Result<Map<String, Object>> getSummaryStats() {
        return Result.success(deviceService.getSummaryStats());
    }


    /**
     * 批量打印设备标签
     */
    @SaCheckPermission("itam:device:query")
    @PostMapping(value = "/label/batch")
    public void printLabels(@RequestBody List<Long> ids, jakarta.servlet.http.HttpServletResponse response) {
        devicePrintService.printLabels(ids, response);
    }

    /**
     * 获取设备详情（包含关联资产）
     *
     * @param id 主键
     */
    @SaCheckPermission("itam:device:query")
    @GetMapping(value = "/detail/{id:\\d+}")
    public Result<DeviceDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(deviceDetailService.getDeviceDetail(id));
    }

}
