import request from '@/utils/http'

export interface AccessoriesEntity extends Api.Common.BaseEntity {
    id: number
    name: string
    assetNumber: string
    categoryId: string
    supplierId: string
    manufacturerId: string
    specifications: string
    locationId: string
    depreciationId: string
    quantity: string
    minQuantity: string
    purchaseDate: string
    warrantyExpirationDate: string
    purchaseCost: string
    orderNumber: string
    serialNumber: string
    description: string
}

type AccessoriesList = Api.Common.PaginatedResponse<AccessoriesEntity>

type AccessoriesSearchFields = {
    id: number
    name: string
    assetNumber: string
    categoryId: string
    supplierId: string
    manufacturerId: string
    specifications: string
    locationId: string
    depreciationId: string
    quantity: string
    minQuantity: string
    purchaseDate: string
    warrantyExpirationDate: string
    purchaseCost: string
    orderNumber: string
    serialNumber: string
    description: string
}

type AccessoriesSearchParams = AccessoriesSearchFields & Api.Common.CommonSearchParams

/**
 * 获取配件列表
 */
export function pageAccessories(params: AccessoriesSearchParams) {
    return request.get<AccessoriesList>({
        url: '/itam/accessories/page',
        params
    })
}

/**
 * 获取配件详情
 * @param id
 */
export function getAccessories(id: number): any {
    return request.get<AccessoriesEntity>({
        url: `/itam/accessories/${id}`
    })
}

/**
 * 创建配件
 * @param data
 */
export function addAccessories(data: AccessoriesEntity) {
    return request.post({
        url: '/itam/accessories/add',
        data
    })
}

/**
 * 修改配件
 * @param data
 */
export function updateAccessories(data: AccessoriesEntity) {
    return request.post({
        url: '/itam/accessories/update',
        data
    })
}

/**
 * 删除配件
 * @param id
 */
export function delAccessories(id: number | number[]) {
    return request.post({
        url: `/itam/accessories/delete/${id}`
    })
}

/**
 * 导出配件
 */
export function exportAccessories(query: AccessoriesSearchParams) {
    return request.get<Blob>({
        url: '/itam/accessories/export',
        responseType: 'blob',
        params: query
    })
}

/**
 * 分配配件到设备
 */
export function allocateAccessory(data: {
    itemId: number;
    ownerId: number;
    ownerType: string;
    quantity: number;
    note?: string
}) {
    return request.post({
        url: '/itam/accessories/allocate',
        data
    })
}

// 配件分配记录类型
export interface AccessoryAllocationEntity extends Api.Common.BaseEntity {
    id: number
    itemType: string
    itemTypeDesc: string
    itemId: number
    itemName: string
    ownerType: string
    ownerTypeDesc: string
    ownerId: number
    ownerName: string
    quantity: number
    status: string
    statusDesc: string
    assignDate: string
    returnDate?: string | null
    note?: string | null
}

type AllocationList = Api.Common.PaginatedResponse<AccessoryAllocationEntity>

type AllocationSearchFields = {
    itemName?: string
    ownerName?: string
    status?: string
}

type AllocationSearchParams = AllocationSearchFields & Api.Common.CommonSearchParams

/**
 * 获取配件分配记录列表（分页）
 */
export function pageAccessoryAllocations(params: AllocationSearchParams) {
    return request.get<AllocationList>({
        url: '/itam/accessories/allocation/page',
        params
    })
}

/**
 * 解除配件关联
 * @param id 分配记录ID
 */
export function deallocateAccessory(id: number) {
    return request.post({
        url: `/itam/accessories/deallocate/${id}`
    })
}

/**
 * 报告配件异常
 */
export function reportAccessoryFailure(data: {
    targetId: number
    failureName?: string
    failureDescription?: string
    failureDate?: string
    notes?: string
}) {
    return request.post({
        url: '/itam/accessories/report',
        data
    })
}

// 配件统计相关类型
export interface AccessoryStatsSummary {
    total: number          // 配件总数
    expired: number        // 已过保数量
    lowStock: number       // 低库存数量
    soonToExpire: number   // 即将过保数量
}

export interface MonthlyValueStat {
    statsMonth: string        // 统计月份
    assetsType: string        // 资产类型
    assetsTypeDesc: string    // 资产类型描述
    totalValue: number        // 总价值
}

/**
 * 获取配件统计摘要
 */
export function getAccessoriesStatsSummary() {
    return request.get<AccessoryStatsSummary>({
        url: '/itam/accessories/stats/summary'
    })
}

/**
 * 获取配件月度价值统计
 */
export function getAccessoriesMonthlyValue() {
    return request.get<MonthlyValueStat[]>({
        url: '/itam/accessories/stats/monthly-value'
    })
}
