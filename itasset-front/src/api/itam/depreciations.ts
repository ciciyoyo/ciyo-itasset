import request from '@/utils/http'

// 规则项类型
export interface StageItem {
    period: number
    unit: string
    ratio: number
}

export interface DepreciationsEntity extends Api.Common.BaseEntity {
    id?: number
    name: string
    stages?: StageItem[]
    remark?: string
}

type DepreciationsList = Api.Common.PaginatedResponse<DepreciationsEntity>

type DepreciationsSearchFields = {
    id?: number
    name?: string
    stages?: string
    remark?: string
}

type DepreciationsSearchParams = DepreciationsSearchFields & Api.Common.CommonSearchParams

/**
 * 获取折旧规则列表（分页）
 */
export function pageDepreciations(params: DepreciationsSearchParams) {
    return request.get<DepreciationsList>({
        url: '/itam/depreciations/page',
        params
    })
}

/**
 * 获取折旧规则列表（不分页）
 */
export function listDepreciations(params?: DepreciationsSearchFields) {
    return request.get<DepreciationsEntity[]>({
        url: '/itam/depreciations/list',
        params
    })
}

/**
 * 获取折旧规则详情
 * @param id
 */
export function getDepreciations(id: number): any {
    return request.get<DepreciationsEntity>({
        url: `/itam/depreciations/${id}`
    })
}

/**
 * 创建折旧规则
 * @param data
 */
export function addDepreciations(data: DepreciationsEntity) {
    return request.post({
        url: '/itam/depreciations/add',
        data
    })
}

/**
 * 修改折旧规则
 * @param data
 */
export function updateDepreciations(data: DepreciationsEntity) {
    return request.post({
        url: '/itam/depreciations/update',
        data
    })
}

/**
 * 删除折旧规则
 * @param id
 */
export function delDepreciations(id: number | number[]) {
    return request.post({
        url: `/itam/depreciations/delete/${id}`
    })
}

/**
 * 导出折旧规则
 */
export function exportDepreciations(query: DepreciationsSearchParams) {
    return request.get<Blob>({
        url: '/itam/depreciations/export',
        responseType: 'blob',
        params: query
    })
}
