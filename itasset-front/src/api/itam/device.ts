import request from '@/utils/http'

export interface DeviceEntity extends Api.Common.BaseEntity {
    id?: number
    modelId: string
    serial: string
    name: string
    imageUrl?: string
    purchaseDate?: string
    purchaseCost?: string
    warrantyDate?: string
    locationId?: string
    supplierId?: string
    depreciationId?: string
    description?: string
    deviceNo: string
}

type DeviceList = Api.Common.PaginatedResponse<DeviceEntity>

type DeviceSearchFields = {
    serial?: string
    name?: string
    deviceNo?: string
}

type DeviceSearchParams = DeviceSearchFields & Api.Common.CommonSearchParams

/**
 * 获取设备列表
 */
export function pageDevice(params: DeviceSearchParams) {
    return request.get<DeviceList>({
        url: '/itam/device/page',
        params
    })
}

/**
 * 获取设备列表
 */
export function listDevice(params: DeviceSearchFields) {
    return request.get<DeviceList>({
        url: '/itam/device/list',
        params
    })
}

/**
 * 获取设备详情
 * @param id
 */
export function getDevice(id: number): any {
    return request.get<DeviceEntity>({
        url: `/itam/device/${id}`
    })
}

/**
 * 创建设备
 * @param data
 */
export function addDevice(data: DeviceEntity) {
    return request.post({
        url: '/itam/device/add',
        data
    })
}

/**
 * 修改设备
 * @param data
 */
export function updateDevice(data: DeviceEntity) {
    return request.post({
        url: '/itam/device/update',
        data
    })
}

/**
 * 删除设备
 * @param id
 */
export function delDevice(id: number | number[]) {
    return request.post({
        url: `/itam/device/delete/${id}`
    })
}

/**
 * 导出设备
 */
export function exportDevice(query: DeviceSearchParams) {
    return request.get<Blob>({
        url: '/itam/device/export',
        responseType: 'blob',
        params: query
    })
}

export interface AllocateData {
    itemType: string
    itemId: number
    ownerType: string
    ownerId: number
    note?: string
}

/**
 * 分配设备
 */
export function allocateDevice(data: AllocateData) {
    return request.post({
        url: '/itam/device/allocate',
        data
    })
}

export interface AllocationEntity extends Api.Common.BaseEntity {
    id: number
    itemId: number
    itemType: string
    itemTypeDesc: string
    itemName: string
    ownerId: number
    ownerType: string
    ownerTypeDesc: string
    ownerName: string
    quantity: number
    status: string
    statusDesc: string
    assignDate: string
    returnDate: string | null
    note: string
}

/**
 * 获取分配列表
 */
export function pageAllocation(params: any) {
    return request.get<Api.Common.PaginatedResponse<AllocationEntity>>({
        url: '/itam/device/allocation/page',
        params
    })
}

/**
 * 报废设备
 */
export function scrappedDevice(ids: number[]) {
    return request.post({
        url: '/itam/device/scrap',
        data: {ids}
    })
}

/**
 * 取消分配
 */
export function deallocateDevice(id: number) {
    return request.post({
        url: `/itam/device/deallocate/${id}`
    })
}

export interface DeviceSummaryVO {
    total: number
    expired: number
    scrapped: number
    soonToExpire: number
}

export interface DeviceMonthlyValueVO {
    statsMonth: string
    totalValue: number
}

/**
 * 获取设备统计概要
 */
export function getDeviceSummary() {
    return request.get<DeviceSummaryVO>({
        url: '/itam/device/stats/summary'
    })
}

/**
 * 获取设备月度价值统计
 */
export function getDeviceMonthlyValue(year?: number) {
    return request.get<DeviceMonthlyValueVO[]>({
        url: '/itam/device/stats/monthly-value',
        params: {year}
    })
}
