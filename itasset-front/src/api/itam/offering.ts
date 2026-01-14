import request from '@/utils/http'
import {FailuresEntity} from './failures'

export interface OfferingEntity extends Api.Common.BaseEntity {
    id: number | null
    name: string
    supplierId: number | null
    serviceNumber: string
    startDate: string
    endDate: string
    cost: number
    notes: string
    offeringStatus: string
    targetType?: string
    targetId?: number
    deleted?: number
}

export interface OfferingVO extends Api.Common.BaseEntity {
    id: number
    name: string
    supplierId: number
    serviceNumber: string
    startDate: string
    endDate: string
    cost: number
    notes: string
    targetType: string | null
    targetId: number
    offeringStatus: string | null
    supplierName: string
    targetName: string | null
    assignDate: string | null
}

type OfferingList = Api.Common.PaginatedResponse<OfferingVO>

export interface OfferingStatisticsVO {
    exceptionCount: number
    normalCount: number
    totalCount: number
    totalAmount: number
}

export interface ServiceValueStatisticVO {
    statsMonth: string
    assetsType: string
    assetsTypeDesc: string
    totalValue: number
}

type OfferingSearchFields = {
    id: number
    name: string
    supplierId: number
    serviceNumber: string
    startDate: string
    endDate: string
    cost: number
    notes: string
    offeringStatus: string
    targetType: string
    targetId: number
    deleted: number
    exceptionStartDate: string
    exceptionEndDate: string
    createBy: string
    updateBy: string
}

type OfferingSearchParams = OfferingSearchFields & Api.Common.CommonSearchParams

/**
 * 获取服务列表
 */
export function pageOffering(params: OfferingSearchParams) {
    return request.get<OfferingList>({
        url: '/itam/offering/page',
        params
    })
}

/**
 * 获取服务详情
 * @param id
 */
export function getOffering(id: number) {
    return request.get<OfferingVO>({
        url: `/itam/offering/${id}`
    })
}

/**
 * 创建服务
 * @param data
 */
export function addOffering(data: OfferingEntity) {
    return request.post({
        url: '/itam/offering/add',
        data
    })
}

/**
 * 修改服务
 * @param data
 */
export function updateOffering(data: OfferingEntity) {
    return request.post({
        url: '/itam/offering/update',
        data
    })
}

/**
 * 删除服务
 * @param id
 */
export function delOffering(id: number | number[]) {
    return request.post({
        url: `/itam/offering/delete/${id}`
    })
}

/**
 * 导出服务
 */
export function exportOffering(query: OfferingSearchFields) {
    return request.get<Blob>({
        url: '/itam/offering/export',
        responseType: 'blob',
        params: query
    })
}

/**
 * 报告异常
 * @param data
 */
export function reportException(data: FailuresEntity) {
    return request.post({
        url: '/itam/offering/report-exception',
        data
    })
}

/**
 * 绑定设备
 * @param data
 */
export function bindAsset(data: { id: number; targetId: number }) {
    return request.post<boolean>({
        url: '/itam/offering/bind-asset',
        data
    })
}

/**
 * 解除服务归属
 * @param data
 */
export function unbindAsset(data: { id: number }) {
    return request.post<boolean>({
        url: '/itam/offering/unbind',
        data
    })
}

/**
 * 解决故障
 * @param data
 */
export function resolveException(data: FailuresEntity) {
    return request.post<boolean>({
        url: '/itam/offering/resolve-exception',
        data
    })
}

/**
 * 获取服务统计信息
 */
export function getOfferingStatistics() {
    return request.get<OfferingStatisticsVO>({
        url: '/itam/offering/statistics'
    })
}

/**
 * 获取每月服务价值统计
 * @param year 年份
 */
export function getServiceValueStatistics(year?: number) {
    return request.get<ServiceValueStatisticVO[]>({
        url: '/itam/offering/statistics/service-value',
        params: {year}
    })
}

export interface SupplierExceptionStatisticsVO {
    supplierId: number
    supplierName: string
    failureCount: number
}

/**
 * 供应商服务异常
 */
export function getSupplierExceptionStatistics() {
    return request.get<SupplierExceptionStatisticsVO[]>({
        url: '/itam/offering/statistics/supplier-failure-stats'
    })
}
