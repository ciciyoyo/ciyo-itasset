package com.ciyocloud.itam.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 设备详情 VO，包含关联的资产信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceDetailVO extends DeviceVO {

    /**
     * 关联的配件列表
     */
    private List<AccessoriesVO> accessories;

    /**
     * 关联的软件授权列表
     */
    private List<LicensesVO> licenses;

    /**
     * 关联的服务列表
     */
    private List<OfferingVO> services;

    /**
     * 关联的耗材列表
     */
    private List<ConsumablesVO> consumables;

    /**
     * 关联的型号信息
     */
    private ModelsVO model;


    // 如果未来需要关联其他设备（如子组件），可以在这里添加
    // private List<DeviceVO> components;
}
