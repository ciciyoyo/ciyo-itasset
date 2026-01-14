package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.itam.entity.DeviceEntity;
import com.ciyocloud.itam.entity.FailuresEntity;
import com.ciyocloud.itam.entity.OfferingEntity;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.enums.DeviceStatus;
import com.ciyocloud.itam.enums.FailureStatus;
import com.ciyocloud.itam.enums.OfferingStatus;
import com.ciyocloud.itam.mapper.FailuresMapper;
import com.ciyocloud.itam.req.FailuresPageReq;
import com.ciyocloud.itam.req.FailuresReportReq;
import com.ciyocloud.itam.service.DeviceService;
import com.ciyocloud.itam.service.FailuresService;
import com.ciyocloud.itam.service.OfferingService;
import com.ciyocloud.itam.vo.FailuresVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 故障表Service业务层处理
 *
 * @author codeck
 * @since 2026-01-01
 */
@Service
@RequiredArgsConstructor
public class FailuresServiceImpl extends ServiceImpl<FailuresMapper, FailuresEntity> implements FailuresService {

    private final OfferingService offeringService;
    private final DeviceService deviceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportFailure(FailuresEntity failure) {
        failure.setStatus(FailureStatus.PENDING);
        failure.setReportedBy(SecurityUtils.getUserId());
        this.save(failure);
        // 如果关联的是服务，更新服务状态为异常
        if (AssetType.SERVICE.equals(failure.getTargetType()) && failure.getTargetId() != null) {
            updateOfferingStatus(failure.getTargetId(), OfferingStatus.EXCEPTION);
        }
        // 如果关联的是设备，更新设备状态为故障
        if (AssetType.DEVICE.equals(failure.getTargetType()) && failure.getTargetId() != null) {
            updateDeviceStatus(failure.getTargetId(), DeviceStatus.FAULT);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportFailure(AssetType targetType, FailuresReportReq req) {
        FailuresEntity failures = new FailuresEntity();
        failures.setTargetType(targetType);
        failures.setTargetId(req.getTargetId());
        failures.setFailureName(req.getFailureName());
        failures.setFailureDescription(req.getFailureDescription());
        failures.setFailureDate(req.getFailureDate());
        failures.setNotes(req.getNotes());
        this.reportFailure(failures);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveFailure(Long id, String notes) {
        FailuresEntity failure = this.getById(id);
        if (failure == null) {
            throw new RuntimeException("故障记录不存在");
        }
        failure.setStatus(FailureStatus.RESOLVED);
        failure.setResolvedDate(LocalDateTime.now());
        failure.setResolvedBy(SecurityUtils.getUserId());
        if (notes != null) {
            failure.setNotes(notes);
        }
        this.updateById(failure);

        // 如果关联的是服务，检查是否还有其他未解决的故障。如果没有了，更新服务状态为正常
        if (AssetType.SERVICE.equals(failure.getTargetType()) && failure.getTargetId() != null) {
            long count = this.lambdaQuery()
                    .eq(FailuresEntity::getTargetType, AssetType.SERVICE)
                    .eq(FailuresEntity::getTargetId, failure.getTargetId())
                    .ne(FailuresEntity::getStatus, FailureStatus.RESOLVED)
                    .ne(FailuresEntity::getStatus, FailureStatus.SCRAPPED)
                    .count();
            if (count == 0) {
                updateOfferingStatus(failure.getTargetId(), OfferingStatus.NORMAL);
            }
        }

        // 如果关联的是设备，检查是否还有其他未解决的故障。如果没有了，更新设备状态
        if (AssetType.DEVICE.equals(failure.getTargetType()) && failure.getTargetId() != null) {
            long count = this.lambdaQuery()
                    .eq(FailuresEntity::getTargetType, AssetType.DEVICE)
                    .eq(FailuresEntity::getTargetId, failure.getTargetId())
                    .ne(FailuresEntity::getStatus, FailureStatus.RESOLVED)
                    .ne(FailuresEntity::getStatus, FailureStatus.SCRAPPED)
                    .count();
            if (count == 0) {
                DeviceEntity device = deviceService.getById(failure.getTargetId());
                if (device != null) {
                    // 如果有分配人，则状态为在用，否则为闲置
                    device.setAssetsStatus(device.getAssignedTo() != null ? DeviceStatus.DEPLOYED : DeviceStatus.PENDING);
                    deviceService.updateById(device);
                }
            }
        }
    }

    private void updateOfferingStatus(Long offeringId, OfferingStatus status) {
        OfferingEntity offering = new OfferingEntity();
        offering.setId(offeringId);
        offering.setOfferingStatus(status);
        offeringService.updateById(offering);
    }

    private void updateDeviceStatus(Long deviceId, DeviceStatus status) {
        DeviceEntity device = new DeviceEntity();
        device.setId(deviceId);
        device.setAssetsStatus(status);
        deviceService.updateById(device);
    }

    @Override
    public Page<FailuresVO> queryPageVo(Page<FailuresVO> page, FailuresPageReq req) {
        QueryWrapper<FailuresEntity> wrapper = new QueryWrapper<>();

        wrapper.eq(req.getTargetType() != null, "t1.target_type", req.getTargetType());
        wrapper.eq(req.getTargetId() != null, "t1.target_id", req.getTargetId());
        wrapper.like(StringUtils.isNotBlank(req.getFailureName()), "t1.failure_name", req.getFailureName());
        wrapper.eq(req.getStatus() != null, "t1.status", req.getStatus());
        wrapper.eq(req.getReportedBy() != null, "t1.reported_by", req.getReportedBy());
        wrapper.eq(req.getResolvedBy() != null, "t1.resolved_by", req.getResolvedBy());

        if (req.getBeginTime() != null) {
            wrapper.ge("t1.failure_date", req.getBeginTime());
        }
        if (req.getEndTime() != null) {
            wrapper.le("t1.failure_date", req.getEndTime());
        }

        wrapper.orderByDesc("t1.failure_date");

        return baseMapper.selectPageVo(page, wrapper);
    }
}
